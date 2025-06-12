package com.matching.ezgg.domain.matching.infra.redis.consumer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.matching.ezgg.domain.matching.infra.redis.service.RedisService;
import com.matching.ezgg.domain.matching.service.MatchingProcessor;
import com.matching.ezgg.domain.review.service.ReviewService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisStreamConsumer {

	private final RedisService redisService;
	private final MatchingProcessor matchingProcessor;
	private final ReviewService reviewService;

	/**
	 * 서버 시작 시 Consumer Group을 생성하는 메서드
	 *
	 * <p><b>동작 단계</b></p>
	 * <ol>
	 *   <li>{@link RedisService#createStringGroup()} 호출로 Consumer Group 생성 시도</li>
	 *   <li>이미 존재한다면 예외를 잡아 로그만 기록하고 넘어감</li>
	 * </ol>
	 *
	 */
	@PostConstruct
	public void init() {
		try {
			redisService.createStringGroup();
			log.info("[INFO] Redis Stream Consumer Group 생성 완료");
		} catch (Exception e) {
			log.info("[INFO] 이미 Consumer Group이 존재합니다.");
		}
	}

	/**
	 * Redis Stream 메시지를 소비해 매칭을 시도하는 스케줄러 메서드
	 *
	 * <p><b>동작 단계</b></p>
	 * <ol>
	 *   <li>1초마다 {@link RedisService#getStringGroup()} 으로 최대 5명의 매칭 후보를 가져옴</li>
	 *   <li>가져온 메시지가 없으면 그대로 반환</li>
	 *   <li>각 메시지에 대해
	 *       <ul>
	 *         <li>삭제 대기 큐 포함 여부 확인 후 포함 시 처리 건너뜀</li>
	 *         <li>{@link MatchingProcessor#tryMatching(Long)} 호출로 매칭 시도</li>
	 *       </ul>
	 *   </li>
	 * </ol>
	 *
	 */
	@Scheduled(fixedDelay = 1000)
	public void consumeStreamMessage() {
		try {
			// 다음으로 매칭을 시도할 유저를 최대 5명까지 stream에서 가져온다.
			List<MapRecord<String, Object, Object>> messages = redisService.getStringGroup();

			// 매칭을 시도할 유저가 없으면 리턴
			if (messages == null || messages.isEmpty()) {
				return;
			}

			log.info("[INFO] 매칭 시도중... (지금 매칭 시도 로직을 돌리고 있는 유저 수 = {})", messages.size());//FIXME 추후 확인할 필요없으면 로그 삭제

			for (MapRecord<String, Object, Object> message : messages) {
				Long memberId = Long.valueOf((String)message.getValue().get("memberId"));

				if (redisService.isInDeleteQueue(memberId)) {
					log.info("[INFO] is in delete queue : memberId={} ", memberId);
					return;
				}
				matchingProcessor.tryMatching(memberId);
			}

		} catch (Exception e) {
			log.error("[ERROR] Redis Stream Consumer 처리 중 에러 발생 : {}", e.getMessage());
		}
	}

	@Scheduled(fixedDelay = 1000 * 60) // 1분마다 실행
	public void findDuoGame() {
		List<Map<String, String>> matchedUsers = redisService.getMatchedUsers();
		if (matchedUsers == null || matchedUsers.isEmpty()) {
			return;
		}

		for (Map<String, String> matchedUser : matchedUsers) {
			Long memberId1 = Long.valueOf(matchedUser.get("memberId1"));
			Long memberId2 = Long.valueOf(matchedUser.get("memberId2"));

			// 20회 카운트 초과 시 매칭된 유저 삭제
			if(!redisService.canExecuteReview(memberId1, memberId2)){
				log.info("[INFO] 매칭된 유저 삭제 : {} - {}", memberId1, memberId2);
				redisService.deleteMatchedUser(matchedUser);
				continue;
			}

			Map<String, String> updateMatchedUser = new HashMap<>();
			updateMatchedUser.put("memberId1", matchedUser.get("memberId1"));
			updateMatchedUser.put("memberId2", matchedUser.get("memberId2"));
			updateMatchedUser.put("timestamp", String.valueOf(System.currentTimeMillis()));

			try {
				redisService.updateMatchedUser(matchedUser, updateMatchedUser);
			}catch (Exception e) {
				log.error("[ERROR] Redis에 매칭된 유저 업데이트 실패: {}", e.getMessage());
			}

			reviewService.findDuoGame(memberId1, memberId2, updateMatchedUser);
		}
	}
}


