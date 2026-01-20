package com.matching.ezgg.global.config;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SessionRegistry {

	private final Set<String> connectedUsers = ConcurrentHashMap.newKeySet();

	/**
	 * 등록된 사용자 ID를 세션 레지스트리에 추가합니다.
	 * @param userId
	 */
	public void register(String userId) {
		connectedUsers.add(userId);
		log.info("[INFO] {}번 유저 웹소켓 연결됨", userId);
	}

	/**
	 * 등록된 사용자 ID를 세션 레지스트리에서 제거합니다.
	 * @param userId
	 */
	public void unregister(String userId) {
		connectedUsers.remove(userId);
		log.info("[INFO] {}번 유저 웹소켓 연결 해제됨", userId);
	}

	/**
	 * 등록된 사용자 ID가 세션 레지스트리에 있는지 확인합니다.
	 * @param userId
	 * @return
	 */
	public boolean isConnected(String userId) {
		return connectedUsers.contains(userId);
	}
}
