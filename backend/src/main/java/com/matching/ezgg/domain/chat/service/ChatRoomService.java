package com.matching.ezgg.domain.chat.service;

import static com.matching.ezgg.domain.chat.util.ChatRoomUtil.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.matching.ezgg.domain.chat.dto.ChatMessageDto;
import com.matching.ezgg.domain.chat.util.ChatRoomUtil;
import com.matching.ezgg.global.config.SessionRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

	private final SessionRegistry sessionRegistry;

	// 중복 떠남 처리 방지용 (10초 내 동일한 떠남 처리 무시)
	private final ConcurrentHashMap<String, Long> recentlyLeftUsers = new ConcurrentHashMap<>();

	// 현재 처리 중인 leave 요청 추적 (동시성 제어)
	private final ConcurrentHashMap<String, Boolean> processingLeave = new ConcurrentHashMap<>();

	/**
	 * 채팅방에 참여자 추가
	 * @param chattingRoomId 채팅방 ID
	 * @param memberId 참여자 ID (유저 ID 또는 username)
	 */
	public void addParticipant(String chattingRoomId, String memberId) {
		if (chattingRoomId == null || memberId == null) {
			log.warn("[WARN] 채팅방 ID 또는 멤버 ID가 null입니다. chattingRoomId: {}, memberId: {}", chattingRoomId, memberId);
			return;
		}

		chatRoomParticipants
			.computeIfAbsent(chattingRoomId, key -> new CopyOnWriteArrayList<>())
			.addIfAbsent(memberId);
	}

	/**
	 * 채팅방에서 참여자 제거
	 * @param chattingRoomId 채팅방 ID
	 * @param memberId 참여자 ID
	 */
	public void removeParticipant(String chattingRoomId, String memberId) {
		if (chattingRoomId == null || memberId == null) {
			log.warn("[WARN] 채팅방 ID 또는 멤버 ID가 null입니다. chattingRoomId: {}, memberId: {}", chattingRoomId, memberId);
			return;
		}

		List<String> participants = chatRoomParticipants.get(chattingRoomId);
		if (participants != null) {
			boolean removed = participants.remove(memberId);
			if (removed) {
				log.info("[INFO] 채팅방 참여자 제거됨 - 방ID: {}, 멤버ID: {}, 현재 참여자 수: {}",
					chattingRoomId, memberId, participants.size());

				// 참여자가 모두 나가면 채팅방 정보 삭제
				if (participants.isEmpty()) {
					chatRoomParticipants.remove(chattingRoomId);
					log.info("[INFO] 채팅방 삭제됨 - 방ID: {} (참여자 0명)", chattingRoomId);
				}
			}
		}
	}

	/**
	 * 상대방에게 유저 떠남을 알리는 메서드 (개선됨)
	 * @param chattingRoomId 채팅방 ID
	 * @param leftUserId 떠난 유저 ID
	 * @param messagingTemplate 메시지 전송용
	 * @param participantsBeforeLeave 떠나기 전 참가자 목록 (미리 계산됨)
	 */
	private void notifyPartnerUserLeft(String chattingRoomId, String leftUserId,
		SimpMessagingTemplate messagingTemplate, List<String> participantsBeforeLeave) {
		// 떠난 유저가 실제로 참가자 목록에 있었는지 확인
		if (!participantsBeforeLeave.contains(leftUserId)) {
			log.warn("[WARN] 떠난 유저 {}가 채팅방 {}에 존재하지 않았음", leftUserId, chattingRoomId);
			return;
		}

		// 혼자 있었던 경우 또는 이미 빈 채팅방인 경우 알림을 보내지 않음
		if (participantsBeforeLeave.size() <= 1) {
			log.info("[INFO] 혼자 있던 사용자가 떠남. 알림 전송하지 않음 - 방ID: {}", chattingRoomId);
			return;
		}

		// 떠나기 전 참가자 목록에서 떠난 유저를 제외한 나머지에게만 알림
		for (String participantId : participantsBeforeLeave) {
			if (!participantId.equals(leftUserId)) {
				// 해당 유저가 여전히 연결되어 있는지 확인
				if (!sessionRegistry.isConnected(participantId)) {
					log.info("[INFO] 유저 {}는 이미 연결 해제됨. 알림 건너뜀", participantId);
					continue;
				}

				try {
					// 상대방에게 유저 떠남 알림 전송 (별도 큐)
					ChatMessageDto notification = ChatMessageDto.builder()
						.type("OPPONENT_LEFT")
						.chattingRoomId(chattingRoomId)  // 채팅방 ID 포함!
						.sender(getDisplayName(leftUserId))
						.message("상대방이 채팅방을 떠났습니다")
						.timestamp(String.valueOf(System.currentTimeMillis()))
						.build();

					messagingTemplate.convertAndSendToUser(
						participantId,
						"/queue/partner-left",
						notification
					);

					log.info("[INFO] 상대방 떠남 알림 전송 완료: {} -> {}", leftUserId, participantId);

					// 2명이서 채팅하던 방인 경우, 알림 받은 사용자도 자동으로 제거
					if (participantsBeforeLeave.size() == 2) {
						// 별도 스레드에서 잠시 후 제거 (알림 전송 후)
						CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS).execute(() -> {
							try {
								removeParticipant(chattingRoomId, participantId);
								log.info("[INFO] 파트너 퇴장으로 인한 자동 제거 완료: {}", participantId);
							} catch (Exception e) {
								log.error("[ERROR] 자동 제거 실패: {}", e.getMessage());
							}
						});
					}

				} catch (Exception e) {
					log.error("[ERROR] 상대방 떠남 알림 전송 실패: {} -> {}, 에러: {}",
						leftUserId, participantId, e.getMessage());
				}
			}
		}
	}

	/**
	 * 유저가 참여 중인 모든 채팅방에서 제거 (연결 해제 시)
	 * @param userId 유저 ID
	 * @param messagingTemplate 메시지 전송용
	 */
	public void removeUserFromAllChatRooms(String userId, SimpMessagingTemplate messagingTemplate) {
		List<String> userChatRooms = getChatRoomsForUser(userId);

		if (userChatRooms.isEmpty()) {
			return;
		}

		for (String chattingRoomId : userChatRooms) {
			handleUserLeave(chattingRoomId, userId, messagingTemplate, "DISCONNECT");
		}
	}

	/**
	 * 유저가 채팅방을 떠날 때의 모든 처리
	 * @param chattingRoomId 채팅방 ID
	 * @param userId 유저 ID
	 * @param messagingTemplate 메시지 전송용
	 * @param reason 떠남 이유 ("MANUAL", "DISCONNECT")
	 */
	public void handleUserLeave(String chattingRoomId, String userId, SimpMessagingTemplate messagingTemplate,
		String reason) {
		String key = chattingRoomId + ":" + userId;
		long currentTime = System.currentTimeMillis();

		// 중복 처리 방지 (10초 내 동일한 유저의 떠남 처리 무시)
		Long lastLeaveTime = recentlyLeftUsers.get(key);
		if (lastLeaveTime != null && (currentTime - lastLeaveTime) < 10000) {
			return;
		}

		// 동시성 제어: 이미 처리 중인 요청이 있으면 건너뜀
		if (processingLeave.putIfAbsent(key, true) != null) {
			return;
		}

		try {
			// 떠나기 전 참가자 목록을 먼저 가져옴 (동기화된 복사본)
			List<String> participantsBeforeLeave = new ArrayList<>(ChatRoomUtil.getParticipants(chattingRoomId));

			// 해당 유저가 실제로 채팅방에 참여 중인지 확인
			if (!participantsBeforeLeave.contains(userId)) {
				log.warn("[WARN] 유저 {}가 채팅방 {}에 참여하지 않음 (이유: {})", userId, chattingRoomId, reason);
				return;
			}

			// 현재 시간 기록
			recentlyLeftUsers.put(key, currentTime);

			log.info("[INFO] {} 유저가 채팅방 {} 을 떠났습니다 (이유: {})", userId, chattingRoomId, reason);

			// 먼저 알림을 보낸 후 참가자 제거 (순서 중요!)
			notifyPartnerUserLeft(chattingRoomId, userId, messagingTemplate, participantsBeforeLeave);

			// 그 다음에 참가자 제거
			removeParticipant(chattingRoomId, userId);

		} finally {
			// 처리 완료 표시
			processingLeave.remove(key);

			// 메모리 정리 (10초 후)
			CompletableFuture.delayedExecutor(10, TimeUnit.SECONDS).execute(() -> {
				recentlyLeftUsers.remove(key);
			});
		}
	}

	/**
	 * 채팅 메시지 처리 (Controller에서 호출)
	 * @param chatMessage 채팅 메시지
	 * @param principal 인증 정보
	 * @param messagingTemplate 메시지 전송용
	 */
	public void processChatMessage(ChatMessageDto chatMessage, Principal principal,
		SimpMessagingTemplate messagingTemplate) {
		String chattingRoomId = chatMessage.getChattingRoomId();
		String sender = chatMessage.getSender();

		// Principal에서 실제 사용자 ID 가져오기
		String actualUserId = extractUserIdFromPrincipal(principal);
		if (actualUserId == null) {
			log.warn("[WARN] Principal에서 사용자 ID를 가져올 수 없음");
			return;
		}

		// 연결 상태 확인
		if (!sessionRegistry.isConnected(actualUserId)) {
			log.warn("[WARN] 사용자 ID {}가 연결되어 있지 않음", actualUserId);
			return;
		}

		// 채팅방 및 참가자 검증
		List<String> participants = getParticipants(chattingRoomId);
		if (!validateChatRoomAndParticipant(chattingRoomId, actualUserId, participants)) {
			return;
		}

		log.debug("[DEBUG] 메시지 발신자: {} (실제 사용자 ID: {})", sender, actualUserId);
		log.info("[INFO] 참가자 목록: {}", participants);

		// 메시지 전송
		sendChatMessageToParticipants(chatMessage, participants, messagingTemplate);
	}

	/**
	 * 채팅방 나가기 처리 (Controller에서 호출)
	 * @param chattingRoomId 채팅방 ID
	 * @param requestUserId 요청된 사용자 ID
	 * @param principal 인증 정보
	 * @param messagingTemplate 메시지 전송용
	 */
	public void processUserLeave(String chattingRoomId, String requestUserId, Principal principal,
		SimpMessagingTemplate messagingTemplate) {
		// Principal에서 실제 사용자 ID 가져오기
		String actualUserId = extractUserIdFromPrincipal(principal);
		if (actualUserId == null) {
			log.warn("[WARN] 채팅방 나가기 요청에 사용자 ID가 없음 - 방ID: {}", chattingRoomId);
			return;
		}

		// 요청된 사용자 ID와 Principal ID 비교
		if (requestUserId != null && !requestUserId.equals(actualUserId)) {
			log.warn("[WARN] 요청된 userId({})와 Principal ID({})가 다름. Principal ID 사용", requestUserId, actualUserId);
		}

		// 입력 검증
		if (chattingRoomId == null) {
			log.warn("[WARN] 채팅방 나가기 요청에 채팅방 ID가 누락됨");
			return;
		}

		// 채팅방 참여 여부 확인
		if (!isUserInChatRoom(chattingRoomId, actualUserId)) {
			log.warn("[WARN] 사용자 ID {}가 채팅방 {}에 참여하지 않음", actualUserId, chattingRoomId);
			return;
		}

		// 연결 상태 확인
		if (!sessionRegistry.isConnected(actualUserId)) {
			log.info("[INFO] 사용자 ID {}는 이미 연결 해제됨. 수동 leave 처리 건너뜀", actualUserId);
			return;
		}

		log.info("[INFO] 사용자가 수동으로 채팅방을 나갑니다 - 방ID: {}, 사용자 ID: {}", chattingRoomId, actualUserId);

		// 실제 나가기 처리
		try {
			handleUserLeave(chattingRoomId, actualUserId, messagingTemplate, "MANUAL");
		} catch (Exception e) {
			log.error("[ERROR] 채팅방 나가기 처리 중 오류: {}", e.getMessage());
		}
	}

	/**
	 * 채팅방 및 참가자 유효성 검증
	 * @param chattingRoomId 채팅방 ID
	 * @param actualUserId 실제 사용자 ID
	 * @param participants 참가자 목록
	 * @return 검증 통과 여부
	 */
	private boolean validateChatRoomAndParticipant(String chattingRoomId, String actualUserId,
		List<String> participants) {
		// 매칭 시스템 정상성 확인
		if (participants.isEmpty()) {
			log.error("[ERROR] 매칭 시스템 오류: 채팅방 {}에 참가자가 없음", chattingRoomId);
			return false;
		}

		if (!participants.contains(actualUserId)) {
			log.error("[ERROR] 매칭 시스템 오류: 사용자 {}가 채팅방 {}에 등록되지 않음", actualUserId, chattingRoomId);
			return false;
		}

		return true;
	}

	/**
	 * 참가자들에게 채팅 메시지 전송
	 * @param chatMessage 채팅 메시지
	 * @param participants 참가자 목록
	 * @param messagingTemplate 메시지 전송용
	 */
	private void sendChatMessageToParticipants(ChatMessageDto chatMessage, List<String> participants,
		SimpMessagingTemplate messagingTemplate) {
		String chattingRoomId = chatMessage.getChattingRoomId();

		try {
			// 브로드캐스트 메시지 전송 (주 전송 방식)
			messagingTemplate.convertAndSend("/topic/chat/" + chattingRoomId, chatMessage);
			log.info("[INFO] 브로드캐스트 메시지 전송됨");

			// 개별 참가자에게 메시지 전송 (백업 전송 방식)
			for (String participantId : participants) {
				try {
					// 해당 참가자가 여전히 연결되어 있는지 확인
					if (sessionRegistry.isConnected(participantId)) {
						messagingTemplate.convertAndSendToUser(
							participantId,
							"/queue/" + chattingRoomId,
							chatMessage
						);
					} else {
						log.debug("[DEBUG] 참가자 {}는 연결되어 있지 않음. 메시지 건너뜀", participantId);
					}
				} catch (Exception e) {
					log.error("[ERROR] 개별 메시지 전송 실패 - 수신자: {}, 에러: {}", participantId, e.getMessage());
				}
			}
		} catch (Exception e) {
			log.error("[ERROR] 채팅 메시지 전송 중 오류 발생: {}", e.getMessage());
		}
	}

}
