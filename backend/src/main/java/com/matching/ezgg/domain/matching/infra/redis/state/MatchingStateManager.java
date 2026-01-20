package com.matching.ezgg.domain.matching.infra.redis.state;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.matching.ezgg.domain.chat.service.ChatRoomService;
import com.matching.ezgg.domain.matching.infra.redis.service.RedisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchingStateManager {

	private final RedisService redisService;
	private final ChatRoomService chatRoomService;

	/**
	 * 유저를 매칭 대기 상태로 등록하는 메서드
	 *
	 * <p><b>동작 단계</b></p>
	 * <ol>
	 *   <li>해당 회원 ID가 삭제 대기 큐에 포함되어 있는지 확인</li>
	 *   <li>포함되어 있으면 삭제 대기 큐에서 제거</li>
	 *   <li>Redis 스트림에 회원 ID를 등록하여 매칭 대기를 시작</li>
	 * </ol>
	 *
	 * @param memberId 매칭 대기 상태로 전환할 회원 ID
	 */
	public void addUserToMatchingState(Long memberId) {
		if (redisService.isInDeleteQueue(memberId)) {
			redisService.deleteMemberToDeleteQueue(memberId);
		}
		redisService.enqueueToMatchingStream(memberId);
	}

	/**
	 * 매칭 대기 상태에서 유저를 제거하는 메서드
	 *
	 * <p><b>동작 단계</b></p>
	 * <ol>
	 *   <li>Redis 스트림과 해시에서 회원 ID를 제거</li>
	 * </ol>
	 *
	 * @param memberId 매칭 대기를 취소할 회원 ID
	 */
	public void removeUserFromMatchingState(Long memberId) {
		redisService.deleteMatchingState(memberId);
	}

	/**
	 * 두 유저의 매칭 완료를 처리하는 메서드
	 *
	 * <p><b>동작 단계</b></p>
	 * <ol>
	 *   <li>양쪽 회원 ID를 모두 Redis 스트림/해시에서 제거</li>
	 *   <li>각 회원에게 매칭 성공 알림을 전송</li>
	 * </ol>
	 *
	 * @param memberId1 첫 번째 회원 ID
	 * @param memberId2 두 번째 회원 ID
	 */
	public void completeMatchingForBothUsers(Long memberId1, Long memberId2) {
		redisService.deleteMatchingState(memberId1);
		redisService.deleteMatchingState(memberId2);

		String chattingRoomId = UUID.randomUUID().toString();

		redisService.sendMatchingSuccessResponse(memberId1, memberId2, chattingRoomId);
		redisService.sendMatchingSuccessResponse(memberId2, memberId1, chattingRoomId);
		// 채팅방에 참여자 추가
		chatRoomService.addParticipant(chattingRoomId, memberId1.toString());
		chatRoomService.addParticipant(chattingRoomId, memberId2.toString());
	}

	/**
	 * 유저를 재시도 상태로 등록하는 메서드
	 *
	 * <p><b>동작 단계</b></p>
	 * <ol>
	 *   <li>재시도 ZSET에 회원 ID를 삽입하거나 갱신.</li>
	 * </ol>
	 *
	 * @param memberId 재시도 큐에 넣을 회원 ID
	 */
	public void addUserToRetryState(Long memberId) {
		redisService.upsertUserToRetrySet(memberId);
	}

	/**
	 * 유저를 재시도 상태에서 제거하는 메서드
	 *
	 * <p><b>동작 단계</b></p>
	 * <ol>
	 *   <li>재시도 ZSET에서 회원 ID를 제거</li>
	 * </ol>
	 *
	 * @param memberId 재시도 큐에서 제거할 회원 ID
	 */
	public void removeUserFromRetryState(Long memberId) {
		redisService.removeUserFromRetrySet(memberId);
	}

	/**
	 * 유저와 관련된 모든 Redis 키를 삭제하는 메서드
	 *
	 * <p><b>동작 단계</b></p>
	 * <ol>
	 *   <li>회원 ID와 연결된 스트림, 해시, 재시도 ZSET 등 모든 키를 일괄 삭제</li>
	 * </ol>
	 *
	 * @param memberId 삭제할 회원 ID
	 */
	public void removeAllRedisKeysByMemberId(Long memberId) {
		redisService.removeMemberFromAllRedisKeys(memberId);
	}
}
