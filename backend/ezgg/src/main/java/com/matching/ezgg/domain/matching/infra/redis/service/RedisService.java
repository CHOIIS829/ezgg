package com.matching.ezgg.domain.matching.infra.redis.service;

import static com.matching.ezgg.domain.matching.infra.redis.key.RedisKey.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matching.ezgg.domain.matching.dto.MatchingSuccessResponse;
import com.matching.ezgg.domain.matching.dto.MemberDataBundleDto;
import com.matching.ezgg.domain.matching.infra.redis.state.CancelData;
import com.matching.ezgg.domain.matching.service.MemberDataBundleService;
import com.matching.ezgg.global.exception.RetrySetException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

	private final StringRedisTemplate stringRedisTemplate;
	private final RedisTemplate<String, String> redisTemplate;
	private final SimpMessagingTemplate messagingTemplate;
	private final MemberDataBundleService memberDataBundleService;
	private final ObjectMapper objectMapper;

	private String getCancelValueFromKey(String memberId) {
		return "matching:cancel:" + memberId;
	}

	public int getCancelCount(Long memberId) {
		String memberIdStr = String.valueOf(memberId);
		String raw = stringRedisTemplate.opsForValue().get(getCancelValueFromKey(memberIdStr));
		if (raw == null) return 0;

		try {
			CancelData data = objectMapper.readValue(raw, CancelData.class);
			long now = System.currentTimeMillis();
			if (now - data.getTimestamp() > Duration.ofHours(24).toMillis()) {
				stringRedisTemplate.delete(getCancelValueFromKey(memberIdStr));
				return 0;
			}
			return data.getCount();
		} catch (Exception e) {
			stringRedisTemplate.delete(getCancelValueFromKey(memberIdStr));
			return 0;
		}
	}

	public int increaseCancelCount(Long memberId) {
		String memberIdStr = String.valueOf(memberId);
		String key = getCancelValueFromKey(memberIdStr);
		String raw = stringRedisTemplate.opsForValue().get(key);
		long now = System.currentTimeMillis();

		CancelData data = new CancelData(1, now);
		if (raw != null) {
			try {
				CancelData existing = objectMapper.readValue(raw, CancelData.class);
				if (now - existing.getTimestamp() <= Duration.ofHours(24).toMillis()) {
					data.setCount(existing.getCount() + 1);
				}
			} catch (Exception ignored) {
				// 손상된 데이터 무시
			}
		}

		try {
			stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(data));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to save cancel count", e);
		}

		return data.getCount();
	}

	public void resetCancelCount(Long memberId) {
		String memberIdStr = String.valueOf(memberId);
		stringRedisTemplate.delete(getCancelValueFromKey(memberIdStr));
	}

	/**
	 * 매칭 요청 유저를 Redis Stream 및 Hash에 등록하는 메서드
	 *
	 * <p><b>동작 단계</b></p>
	 * <ol>
	 *   <li>회원 ID를 문자열로 변환</li>
	 *   <li>해시( memberId → streamId )에 이미 존재하면 중복 등록을 방지하고 바로 반환</li>
	 *   <li>Stream(XADD)으로 memberId 메시지를 추가</li>
	 *   <li>반환된 RecordId가 null이면 예외를 발생시켜 트랜잭션 오류를 방어</li>
	 *   <li>해시( STREAM_ID_HASH_KEY )에 memberId–streamId 매핑을 저장</li>
	 * </ol>
	 *
	 * @param memberId 매칭을 시작한 회원 ID
	 */
	public void enqueueToMatchingStream(Long memberId) {
		try {
			// redisTemplate 직렬화 일관성을 위해 Long -> String 형태로 변환
			String memberIdStr = String.valueOf(memberId);

			// Hash에 유저가 이미 매칭 중으로 저장되어 있으면 리턴
			if (redisTemplate.opsForHash().hasKey(STREAM_ID_HASH_KEY.getValue(), memberIdStr)) {
				return;
			}

			// Stream에 memberId 메세지 저장
			Map<String, String> message = Map.of("memberId", memberIdStr);
			RecordId recordId = redisTemplate.opsForStream().add(STREAM_KEY.getValue(), message);

			// XADD 실패(트랜잭션/파이프라인 등) 방어
			if (recordId == null) {
				throw new IllegalStateException("XADD 명령이 실패하여 recordId가 null입니다.");
			}

			// Hash에 memberId-streamId 매핑해 저장
			redisTemplate.opsForHash().put(STREAM_ID_HASH_KEY.getValue(), memberIdStr, recordId.getValue());

			log.info("[INFO] Redis Stream 매칭 요청 등록 완료 : memberId={}, streamId={}", memberIdStr, recordId.getValue());
		} catch (IllegalStateException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Stream 등록 실패", e);
		}
	}

	/**
	 * Redis Stream/Hash에서 매칭 유저를 제거하는 메서드
	 *
	 * <p><b>동작 단계</b></p>
	 * <ol>
	 *   <li>해시에서 memberId에 대응하는 streamId를 조회</li>
	 *   <li>조회 결과가 null이 아니면
	 *       <ul>
	 *         <li>XACK로 메시지를 처리 완료( ack ) 상태로 표시.</li>
	 *         <li>XDEL로 스트림에서 실제 메시지를 삭제.</li>
	 *         <li>해시 매핑도 함께 제거</li>
	 *       </ul>
	 *   </li>
	 * </ol>
	 *
	 * @param memberId 삭제 대상 회원 ID
	 */
	public void deleteMatchingState(Long memberId) {
		try {
			String memberIdStr = String.valueOf(memberId);
			// (Hash) memberId → streamId 매핑 조회
			String streamId = (String)redisTemplate.opsForHash()
				.get(STREAM_ID_HASH_KEY.getValue(), memberIdStr);

			if (streamId != null) {
				// (Stream) XACK : 컨슈머 그룹에서 ack(처리 완료)로 표시
				redisTemplate.opsForStream()
					.acknowledge(STREAM_KEY.getValue(), STREAM_GROUP.getValue(), streamId);
				// (Stream) XDEL : 실제 스트림에서 메시지 데이터 제거
				redisTemplate.opsForStream().delete(STREAM_KEY.getValue(), streamId);
				// (Hash) memberId ↔ streamId 매핑 제거
				redisTemplate.opsForHash().delete(STREAM_ID_HASH_KEY.getValue(), memberIdStr);
			}
		} catch (Exception e) {
			log.error("[ERROR] Redis Stream/Hash에서 유저 삭제 처리 중 에러 발생 : {}", e.getMessage());
		}
	}

	/**
	 * 매칭 성공 응답 객체를 생성하는 메서드
	 *
	 * <p><b>동작 단계</b></p>
	 * <ol>
	 *   <li>MemberDataBundleService를 통해 상대 회원의 상세 데이터를 조회</li>
	 *   <li>조회한 데이터를 MatchingSuccessResponse 형태로 빌드</li>
	 * </ol>
	 *
	 * @param matchedMemberId 매칭된 상대 회원 ID
	 * @return {@link MatchingSuccessResponse} 객체
	 */
	private MatchingSuccessResponse buildMatchingSuccessResponse(Long matchedMemberId, String chattingRoomId) {
		MemberDataBundleDto data = memberDataBundleService.getMemberDataBundleByMemberId(matchedMemberId);

		log.info("[INFO] 매칭 성공! >>>>> : {}, 매칭된 방번호 : {}", matchedMemberId, chattingRoomId);

		return MatchingSuccessResponse.builder()
			.status("SUCCESS")
			.data(MatchingSuccessResponse.MatchedMemberData.builder()
				.matchedMemberId(matchedMemberId)
				.memberInfoDto(data.getMemberInfoDto())
				.chattingRoomId(chattingRoomId)
				.recentTwentyMatchDto(data.getRecentTwentyMatchDto())
				.build())
			.build();
	}

	/**
	 * 매칭 성공 메시지를 WebSocket으로 전송하는 메서드
	 *
	 * <p><b>동작 단계</b></p>
	 * <ol>
	 *   <li>{@link #buildMatchingSuccessResponse}로 응답 객체를 생성</li>
	 *   <li>SimpMessagingTemplate.convertAndSendToUser()로 /queue/matching 엔드포인트에 전송</li>
	 * </ol>
	 *
	 * @param memberId        메시지를 받을 회원 ID
	 * @param matchedMemberId 매칭된 상대 회원 ID
	 */
	public void sendMatchingSuccessResponse(Long memberId, Long matchedMemberId, String chattingRoomId) {
		messagingTemplate.convertAndSendToUser(memberId.toString(), "/queue/matching",
			buildMatchingSuccessResponse(matchedMemberId, chattingRoomId));
	}

	/**
	 * 유저를 재시도 ZSET에 upsert(삽입 또는 score 갱신)하는 메서드
	 *
	 * <p><b>동작 단계</b></p>
	 * <ol>
	 *   <li>현재 시각으로부터 10 초 뒤를 score로 계산</li>
	 *   <li>ZADD 수행 후 삽입/갱신 여부를 로그로 남김</li>
	 *   <li>Redis 접속/작업 오류 발생 시 {@link RetrySetException}으로 래핑</li>
	 * </ol>
	 *
	 * @param memberId 재시도 큐에 넣을 회원 ID
	 */
	public void upsertUserToRetrySet(Long memberId) {
		String memberIdStr = String.valueOf(memberId);

		// 다음 재시도 시각 설정
		long retryTime = System.currentTimeMillis() + 10000; // 10초 후 재시도
		long delaySec = (retryTime - System.currentTimeMillis()) / 1000; // 실제 retry 할 때 까지 남은 초

		try {
			// ZADD : 존재하면 score 갱신, 없으면 삽입 (upsert)
			Boolean added = redisTemplate.opsForZSet().add(RETRY_ZSET_KEY.getValue(), memberIdStr, retryTime);

			if (Boolean.TRUE.equals(added)) {
				log.info("[INFO] Retry ZSET에 새 엔트리 추가 : memberId={}, {}초 후 재시도 예정", memberIdStr, delaySec);
			} else {
				log.warn("[WARN] Retry ZSET 엔트리 갱신(중복 발생!) : memberId={}, 재시도 시각을 {}초 뒤로 연장", memberIdStr, delaySec);
			}

		} catch (RedisConnectionFailureException e) {
			log.error("[ERROR] Redis 연결 실패: {}", e.getMessage(), e);
			throw new RetrySetException(memberId, "Redis 연결 실패", e);

		} catch (DataAccessException e) {
			log.error("[ERROR] Redis 작업 실패: {}", e.getMessage(), e);
			throw new RetrySetException(memberId, "Redis 접근 실패", e);
		}
	}

	/**
	 * Redis Stream에 Consumer Group을 생성하는 메서드
	 * <br>해당 그룹은 ReadOffset.latest() 기준으로 메세지를 읽기 시작(즉 현재 시점 이후에 추가되는 메시지부터 읽는다.)
	 */
	public void createStringGroup() {
		stringRedisTemplate.opsForStream().createGroup(
			STREAM_KEY.getValue(), ReadOffset.latest(), STREAM_GROUP.getValue());
	}

	/**
	 * Consumer Group에서 스트림 메시지를 읽어오는 메서드
	 *
	 * <p><b>동작 단계</b></p>
	 * <ol>
	 *   <li>Consumer(group, consumerName) 지정 후</li>
	 *   <li>StreamReadOptions로 최대 5건, 최대 2초 블록(Long polling) 설정</li>
	 *   <li>마지막으로 소비한 offset부터 읽어 온다.</li>
	 * </ol>
	 *
	 * @return 읽어 온 {@code List<MapRecord<...>>}
	 */
	public List<MapRecord<String, Object, Object>> getStringGroup() {
		// MapRecord<Stream명, 유저의 RecordId, 유저의 record 속 key-value 값 {"memberId": "1234"}>

		return stringRedisTemplate.opsForStream().read(
			// Consumer<K>: 지정된 groupName과 consumerName으로 Stream을 읽음.
			Consumer.from(STREAM_GROUP.getValue(), CONSUMER_NAME.getValue()),

			// StreamReadOptions:
			StreamReadOptions
				.empty()
				.count(5) // 최대 5의 메세지(유저)를 읽음
				.block(Duration.ofMillis(2000)),// 메시지가 없을 경우 최대 2초까지 대기 (long polling)

			// StreamOffset<K>: 해당 Consumer가 이전에 읽고 ACK하지 않은 메시지를 우선 읽고, 마지막으로 읽은 메세지 이후부터 읽음.
			StreamOffset.create(STREAM_KEY.getValue(), ReadOffset.lastConsumed())
		);
	}

	/**
	 * 재시도 가능 시각이 지난(10초 이상 대기한) 회원을 조회하는 메서드
	 *
	 * @return score가 현재 시각 이하인 회원 ID 집합
	 */
	public Set<String> getRetryCandidates() {
		long now = System.currentTimeMillis();
		return redisTemplate.opsForZSet().rangeByScore(RETRY_ZSET_KEY.getValue(), 0, now);
	}

	/**
	 * 재시도 ZSET에 등록된 모든 회원을 조회하는 메서드
	 *
	 * @return 회원 ID 집합
	 */
	public Set<String> getAllCandidates() {
		return redisTemplate.opsForZSet().range(RETRY_ZSET_KEY.getValue(), 0, -1);
	}

	/**
	 * 재시도 ZSET에서 회원을 제거하는 메서드
	 * <br>ZREM 수행 후 삭제 결과를 로그로 남긴다.
	 *
	 * @param memberId 제거할 회원 ID
	 */
	public void removeUserFromRetrySet(Long memberId) {
		String memberIdStr = String.valueOf(memberId);
		Long removed = redisTemplate.opsForZSet().remove(RETRY_ZSET_KEY.getValue(), memberIdStr);

		if (removed != null && removed > 0) {
			log.info("[INFO] Retry ZSET에서 유저 삭제 완료: memberId={}", memberIdStr);
		} else {
			log.info("[INFO] Retry ZSET에 유저 삭제 시도 했으나 대상이 없음: memberId={}", memberIdStr);
		}
	}

	/**
	 * 회원과 관련된 모든 Redis 키를 일괄 삭제하는 메서드
	 *
	 * @param memberId 대상 회원 ID
	 */
	public void removeMemberFromAllRedisKeys(Long memberId) {
		deleteMatchingState(memberId); // Hash 삭제, Stream 삭제 및 ack 처리
		removeUserFromRetrySet(memberId); // ZSet 삭제
	}

	/**
	 * 삭제 대기 큐에 회원을 등록하는 메서드
	 *
	 * <p><b>동작 단계</b></p>
	 * <ol>
	 *   <li>SET에 회원 ID를 추가하고 TTL(15 초)을 설정한다.</li>
	 *   <li>오류 발생 시 런타임 예외로 래핑한다.</li>
	 * </ol>
	 *
	 * @param memberId 회원 ID
	 */
	public void addToDeleteQueue(Long memberId) {
		try {
			redisTemplate.opsForSet().add(DELETE_QUEUE_KEY.getValue(), memberId.toString());
			// 딜리트 큐에서 ttl 설정
			redisTemplate.expire(DELETE_QUEUE_KEY.getValue(), Duration.ofSeconds(15));
			log.info("[INFO] 사용자 ID {}가 딜리트 큐에 추가되었습니다.", memberId);
		} catch (Exception e) {
			log.error("[ERROR] Redis에서 딜리트 큐에 오류 발생: {}", e.getMessage());
			throw new RuntimeException("매칭 취소 처리 중 오류가 발생했습니다.", e);
		}
	}

	/**
	 * 회원이 삭제 대기 큐에 등록되어 있는지 확인하는 메서드
	 *
	 * @param memberId 회원 ID
	 * @return 큐에 존재하면 {@code true}, 그렇지 않으면 {@code false}
	 */
	public boolean isInDeleteQueue(Long memberId) {
		Boolean isMember = redisTemplate.opsForSet()
			.isMember(DELETE_QUEUE_KEY.getValue(), memberId.toString());
		return Boolean.TRUE.equals(isMember);
	}

	/**
	 * 삭제 대기 큐에서 회원을 제거하는 메서드
	 *
	 * @param memberId 회원 ID
	 */
	public void deleteMemberToDeleteQueue(Long memberId) {
		redisTemplate.opsForSet().remove(DELETE_QUEUE_KEY.getValue(), memberId.toString());
	}

	public void addToMatchedUsers(Long memberId1, Long memberId2) {
		try {
			String timestamp = String.valueOf(System.currentTimeMillis());
			Map<String, String> matchedData = new HashMap<>();
			matchedData.put("memberId1", String.valueOf(memberId1));
			matchedData.put("memberId2", String.valueOf(memberId2));
			matchedData.put("timestamp", timestamp);

			String json = objectMapper.writeValueAsString(matchedData);

			redisTemplate.opsForZSet().add(MATCHED_ZSET_KEY.getValue(), json, Long.parseLong(timestamp));
		} catch (Exception e) {
			log.error("[ERROR] Redis에 매칭된 유저 추가 실패: {}", e.getMessage());
		}
	}

	public List<Map<String, String>> getMatchedUsers() {
		long now = System.currentTimeMillis();
		long threshold = now - 1000 * 60 * 5; // 10분 전 시간

		Set<String> matchedUsers = redisTemplate.opsForZSet()
			.rangeByScore(MATCHED_ZSET_KEY.getValue(), 0, threshold);

		if (matchedUsers == null || matchedUsers.isEmpty()) {
			return Collections.emptyList();
		}

		List<Map<String, String>> result = new ArrayList<>();

		for (String json : matchedUsers) {
			try {
				Map<String, String> matchedData = objectMapper.readValue(json, Map.class);
				result.add(matchedData);
			} catch (JsonProcessingException e) {
				log.error("[ERROR] 매칭된 유저 데이터 파싱 실패: {}", e.getMessage());
			}
		}
		return result;
	}

	public void updateMatchedUser(Map<String, String> json, Map<String, String> updateJson) {
		try {
			redisTemplate.opsForZSet().remove(MATCHED_ZSET_KEY.getValue(), objectMapper.writeValueAsString(json));
			redisTemplate.opsForZSet().add(MATCHED_ZSET_KEY.getValue(), objectMapper.writeValueAsString(updateJson), System.currentTimeMillis());
		} catch (Exception e) {
			log.error("[ERROR] Redis에 매칭된 유저 업데이트 실패: {}", e.getMessage());
		}
	}

	public void deleteMatchedUser(Map<String, String> deleteJson) {
		try {
			redisTemplate.opsForZSet().remove(MATCHED_ZSET_KEY.getValue(), objectMapper.writeValueAsString(deleteJson));
		} catch (Exception e) {
			log.error("[ERROR] Redis에 매칭된 유저 삭제 실패: {}", e.getMessage());
		}
	}

	public boolean canExecuteReview(Long memberId1, Long memberId2) {
		log.info("[INFO] canExecuteReview 호출: memberId1={}, memberId2={}", memberId1, memberId2);
		String key = "canExecuteReview:" + memberId1 + ":" + memberId2;
		Long count = redisTemplate.opsForValue().increment(key);
		if(count == 1) {
			// 최초 실행일경우
			redisTemplate.expire(key, Duration.ofHours(2)); // 2시간 후 만료
		}
		return count <= 20;
	}
}
