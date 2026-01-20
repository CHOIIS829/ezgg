package com.matching.ezgg.domain.chat.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.matching.ezgg.domain.chat.dto.ChatMessageDto;
import com.matching.ezgg.domain.chat.service.ChatRoomService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

	private final SimpMessagingTemplate messagingTemplate;
	private final ChatRoomService chatRoomService;

	/**
	 * 채팅 메시지 전송 처리
	 * @param chatMessage 채팅 메시지
	 * @param principal 인증 정보
	 */
	@MessageMapping("/chat/send")
	public void sendChatMessage(@Payload ChatMessageDto chatMessage, Principal principal) {
		log.info("[INFO] 채팅 메시지 수신 - 방ID: {}, 발신자: {}, 메시지: {}",
			chatMessage.getChattingRoomId(), chatMessage.getSender(), chatMessage.getMessage());

		chatRoomService.processChatMessage(chatMessage, principal, messagingTemplate);
	}

	/**
	 * 채팅방 나가기 처리
	 * @param payload 요청 데이터
	 * @param principal 인증 정보
	 */
	@MessageMapping("/chat/leave")
	public void leaveChatRoom(@Payload Map<String, String> payload, Principal principal) {
		String chattingRoomId = payload.get("chattingRoomId");
		String userId = payload.get("userId");

		log.info("[INFO] 채팅방 나가기 요청 - 방ID: {}, 요청 사용자: {}", chattingRoomId, userId);

		chatRoomService.processUserLeave(chattingRoomId, userId, principal, messagingTemplate);
	}
}

