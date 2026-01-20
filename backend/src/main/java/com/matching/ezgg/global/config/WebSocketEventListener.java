package com.matching.ezgg.global.config;

import java.security.Principal;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import com.matching.ezgg.domain.chat.service.ChatRoomService;
import com.matching.ezgg.domain.chat.util.ChatRoomUtil;
import com.matching.ezgg.domain.review.service.ReviewNotificationService;
import com.matching.ezgg.global.common.StompPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

	private final SessionRegistry sessionRegistry;
	private final ReviewNotificationService reviewNotificationService;
	private final SimpMessagingTemplate messagingTemplate;
	private final ChatRoomService chatRoomService;

	// 연결 해제 후 대기 중인 사용자들 추적 (userId -> 스케줄된 작업)
	private final ConcurrentHashMap<String, java.util.concurrent.ScheduledFuture<?>> pendingDisconnections = new ConcurrentHashMap<>();
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

	// 대기 시간 (초)
	private static final int DISCONNECT_WAIT_TIME = 3;

	@EventListener
	public void handleWebSocketConnectListener(SessionConnectedEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		Principal principal = accessor.getUser();

		if (principal instanceof StompPrincipal) {
			String userId = principal.getName();

			// 재연결된 경우 대기 중인 연결 해제 작업 취소
			ScheduledFuture<?> pendingTask = pendingDisconnections.remove(userId);
			if (pendingTask != null) {
				pendingTask.cancel(false);
				log.info("[INFO] {} 유저 재연결됨 - 연결 해제 작업 취소", userId);
			} else {
				log.info("[INFO] {} 유저 웹소켓 연결됨", userId);
			}

			sessionRegistry.register(userId);
		}
	}

	@EventListener
	public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		Principal principal = accessor.getUser();
		String destination = accessor.getDestination();

		if (principal instanceof StompPrincipal && destination != null && destination.startsWith(
			"/user/queue/review")) {
			String userId = principal.getName();
			log.info("[INFO] {} 유저가 리뷰 큐를 구독함", userId);

			// 리뷰 확인 후 대기열에서 메시지를 소비
			List<String> pendingReviews = reviewNotificationService.consumePendingReviews(userId);
			if (!pendingReviews.isEmpty()) {
				pendingReviews.forEach(string -> {
					try {
						log.info("[INFO] {} 유저에게 리뷰 요청 전송: {}", userId, string);
						messagingTemplate.convertAndSendToUser(
							userId,
							"/queue/review",
							string
						);
					} catch (Exception e) {
						log.error("[ERROR] 리뷰 요청 전송 중 에러 발생: {}", e.getMessage());
					}
				});
			} else {
				log.info("[INFO] {} 유저 작성할 리뷰 없음", userId);
			}
		}
	}

	@EventListener
	public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		Principal principal = accessor.getUser();

		if (principal instanceof StompPrincipal) {
			String userId = principal.getName();
			sessionRegistry.unregister(userId);
			log.info("[INFO] {} 유저 웹소켓 연결 해제됨", userId);

			// 사용자가 참여 중인 채팅방이 있는지 먼저 확인
			List<String> userChatRooms = ChatRoomUtil.getChatRoomsForUser(userId);

			if (!userChatRooms.isEmpty()) {
				log.info("[INFO] {} 유저가 {} 개의 채팅방에 참여 중 - {}초 후 제거 예정",
					userId, userChatRooms.size(), DISCONNECT_WAIT_TIME);

				// 3초 후 실행될 작업 스케줄링
				ScheduledFuture<?> task = scheduler.schedule(() -> {
					// 다시 연결되었는지 확인
					if (sessionRegistry.isConnected(userId)) {
						log.info("[INFO] {} 유저가 이미 재연결됨 - 제거 작업 취소", userId);
						return;
					}

					log.info("[INFO] {} 유저 {}초 대기 후 채팅방에서 제거 시작", userId, DISCONNECT_WAIT_TIME);

					// 채팅방에서 제거
					List<String> currentChatRooms = ChatRoomUtil.getChatRoomsForUser(userId);
					for (String chattingRoomId : currentChatRooms) {
						try {
							if (ChatRoomUtil.isUserInChatRoom(chattingRoomId, userId)) {
								chatRoomService.handleUserLeave(chattingRoomId, userId, messagingTemplate,
									"DISCONNECT");
							}
						} catch (Exception e) {
							log.error("[ERROR] 채팅방 {} 에서 유저 {} 제거 중 오류: {}",
								chattingRoomId, userId, e.getMessage());
						}
					}

					// 완료 후 맵에서 제거
					pendingDisconnections.remove(userId);

				}, DISCONNECT_WAIT_TIME, TimeUnit.SECONDS);

				// 스케줄된 작업 저장
				pendingDisconnections.put(userId, task);

			} else {
				log.info("[INFO] {} 유저가 참여 중인 채팅방이 없어 추가 처리 없음", userId);
			}
		}
	}
}
