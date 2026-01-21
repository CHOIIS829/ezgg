package com.matching.ezgg.domain.review.service;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.matching.ezgg.domain.matching.infra.redis.key.RedisKey;
import com.matching.ezgg.global.config.SessionRegistry;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewNotificationService {

	private final SimpMessagingTemplate messagingTemplate;
	private final RedisTemplate<String, String> redisTemplate;
	private final SessionRegistry sessionRegistry;

	/**
	 * 웹소켓 연결된 사용자에게 리뷰 요청을 전송하거나, 연결되지 않은 사용자에게는 Redis에 대기열로 저장합니다.
	 * @param memberId1
	 * @param memberId2
	 * @param riotUsername1
	 * @param riotUsername2
	 */
	public void sendOrQueueReview(Long memberId1, String riotUsername1, Long memberId2, String riotUsername2, String matchId) {
		redisTemplate.opsForList().rightPush(RedisKey.REVIEW_PENDING_KEY.getValue() + memberId1, riotUsername2 + "," + matchId);
		redisTemplate.opsForList().rightPush(RedisKey.REVIEW_PENDING_KEY.getValue() + memberId2, riotUsername1 + "," + matchId);

		if(sessionRegistry.isConnected(String.valueOf(memberId1))) {
			messagingTemplate.convertAndSendToUser(
				String.valueOf(memberId1),
				"/queue/review",
				riotUsername2 + "," + matchId
			);
		}
		if(sessionRegistry.isConnected(String.valueOf(memberId2))) {
			messagingTemplate.convertAndSendToUser(
				String.valueOf(memberId2),
				"/queue/review",
				riotUsername1 + "," + matchId
			);
		}
	}

	/**
	 * 해당유저의 리뷰 대기열에서 메시지를 소비합니다.
	 * @param memberId
	 * @return 리뷰 대기열에서 소비된 메시지 리스트
	 */
	public List<String> consumePendingReviews(String memberId) {
		List<String> message = redisTemplate.opsForList().range(RedisKey.REVIEW_PENDING_KEY.getValue() + memberId, 0, -1);
		if (message == null || message.isEmpty()) {
			return List.of(); // 대기열이 비어있으면 빈 리스트 반환
		}
		return message;
	}

	/**
	 * 해당 유저의 리뷰 대기열에서 특정 메시지를 삭제합니다.
	 * @param memberId
	 */
	public void deletePendingReview(Long memberId, String riotUsername, String matchId) {
		List<String> message = redisTemplate.opsForList().range(RedisKey.REVIEW_PENDING_KEY.getValue() + memberId, 0, -1);
		message.forEach(review -> {
			if (review.equals(riotUsername + "," + matchId)) {
				redisTemplate.opsForList().remove(RedisKey.REVIEW_PENDING_KEY.getValue() + memberId, 1, review);
			}
		});
	}
}
