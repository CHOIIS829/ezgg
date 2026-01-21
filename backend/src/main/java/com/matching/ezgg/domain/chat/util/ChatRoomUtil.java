package com.matching.ezgg.domain.chat.util;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.extern.slf4j.Slf4j;

/**
 * 채팅방 관련 유틸리티 클래스
 */
@Slf4j
public final class ChatRoomUtil {

	// 채팅방 ID → 참여자 리스트 (유저 ID or username)
	public static final ConcurrentHashMap<String, CopyOnWriteArrayList<String>> chatRoomParticipants = new ConcurrentHashMap<>();

	/**
	 * 표시용 이름 가져오기 (숫자 ID면 "상대방"으로 표시)
	 */
	public static String getDisplayName(String userId) {
		return isNumericId(userId) ? "상대방" : userId;
	}

	private static boolean isNumericId(String userId) {
		return userId != null && userId.matches("\\d+");
	}

	/**
	 * 특정 유저가 참여 중인 채팅방 목록 반환
	 * @param memberId 유저 ID
	 * @return 참여 중인 채팅방 ID 리스트
	 */
	public static List<String> getChatRoomsForUser(String memberId) {
		List<String> userChatRooms = new ArrayList<>();

		for (Map.Entry<String, CopyOnWriteArrayList<String>> entry : chatRoomParticipants.entrySet()) {
			String chattingRoomId = entry.getKey();
			List<String> participants = entry.getValue();

			if (participants.contains(memberId)) {
				userChatRooms.add(chattingRoomId);
			}
		}
		return userChatRooms;
	}

	/**
	 * 채팅방 참여자 목록 반환
	 * @param chattingRoomId 채팅방 ID
	 * @return 참여자 ID 리스트
	 */
	public static List<String> getParticipants(String chattingRoomId) {
		if (chattingRoomId == null) {
			log.warn("[WARN] 채팅방 ID가 null입니다.");
			return new CopyOnWriteArrayList<>();
		}

		List<String> participants = chatRoomParticipants.getOrDefault(chattingRoomId, new CopyOnWriteArrayList<>());
		log.info("[INFO] 채팅방 참여자 조회 - 방ID: {}, 참여자 수: {}, 참여자 목록: {}",
			chattingRoomId, participants.size(), participants);

		return participants;
	}

	/**
	 * Principal에서 사용자 ID 추출
	 * @param principal Principal 객체
	 * @return 사용자 ID (숫자)
	 */
	public static String extractUserIdFromPrincipal(Principal principal) {
		if (principal != null) {
			return principal.getName(); // 숫자 ID (예: "2", "3")
		}
		return null;
	}

	/**
	 * 특정 유저가 특정 채팅방에 참여 중인지 확인
	 * @param chattingRoomId 채팅방 ID
	 * @param userId 유저 ID
	 * @return 참여 여부
	 */
	public static boolean isUserInChatRoom(String chattingRoomId, String userId) {
		List<String> participants = getParticipants(chattingRoomId);
		return participants.contains(userId);
	}

}
