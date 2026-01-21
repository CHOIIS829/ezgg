package com.matching.ezgg.domain.matching.infra.redis.scheduler;

import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.matching.ezgg.domain.matching.infra.redis.service.RedisService;
import com.matching.ezgg.domain.matching.infra.redis.state.MatchingStateManager;
import com.matching.ezgg.global.exception.RetrySetException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchingRetryScheduler {

	private final RedisService redisService;
	private final MatchingStateManager matchingStateManager;

	/**
	 * Redis의 매칭 재시도 큐(ZSET)에 있는 유저들을 주기적으로 확인하고,
	 * 일정 시간이 지나 매칭이 가능한 상태라면 매칭 Stream에 다시 등록하는 작업을 수행하는 스케쥴러 메서드.
	 *
	 * <p>동작 방식:</p>
	 * <ul>
	 *   <li>3초마다 실행됨 (@Scheduled)</li>
	 *   <li>DeleteQueue(삭제 대기 큐)에 포함된 유저는 Retry 큐에서 제거</li>
	 *   <li>그 외 유저는 Retry 큐에서 제거하고 Matching Stream + Hash에 등록</li>
	 * </ul>
	 *
	 * <p>예외 발생 시 각 유저 단위로 개별 처리되며, 전체 루프는 중단되지 않음.</p>
	 */
	@Scheduled(fixedDelay = 3000)
	public void processRetryQueue() {
		Set<String> retryCandidates = redisService.getRetryCandidates();

		if (retryCandidates == null || retryCandidates.isEmpty()) {
			return;
		}

		for (String memberIdStr : retryCandidates) {
			Long memberId = Long.parseLong(memberIdStr);

			try {
				// 딜리트 큐에 해당 id가 있는지 확인
				if (redisService.isInDeleteQueue(memberId)) {
					log.info("[INFO] DeleteQueue에 유저가 있어 Retry 처리 중단: memberId={}", memberId);
					// 딜리트 큐에 있으면 해당 id는 매칭 취소된 상태이므로 retry 큐에서 제거
					redisService.removeUserFromRetrySet(memberId);
				} else {
					//딜리트 큐에 없으면 retry zset에서 삭제하고 stream/hash에 저장
					matchingStateManager.addUserToMatchingState(memberId);
					redisService.removeUserFromRetrySet(memberId);
					log.info("[INFO] Retry 처리 완료: memberId={}", memberId);
				}
			} catch (DataAccessException e) {
				log.error("[ERROR] Retry 처리 중 Redis 접근 실패: {}", e.getMessage(), e);
				throw new RetrySetException(memberId, "Redis 접근 실패", e);

			} catch (Exception e) {
				log.error("[ERROR] Retry 처리 중 알 수 없는 에러 발생 : {}", e.getMessage());
			}
		}
	}
}
