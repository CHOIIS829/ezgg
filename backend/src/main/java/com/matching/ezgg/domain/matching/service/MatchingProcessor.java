package com.matching.ezgg.domain.matching.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.matching.ezgg.domain.matching.infra.es.index.MatchingUserDocument;
import com.matching.ezgg.domain.matching.infra.es.service.EsMatchingUserFinder;
import com.matching.ezgg.domain.matching.infra.es.service.ElasticSearchService;
import com.matching.ezgg.domain.matching.infra.redis.service.RedisService;
import com.matching.ezgg.domain.matching.infra.redis.state.MatchingStateManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingProcessor {

	private final EsMatchingUserFinder esMatchingUserFinder;
	private final ElasticSearchService elasticSearchService;
	private final MatchingStateManager matchingStateManager;
	private final RedisService redisService;

	/**
	 * 주어진 {@code memberId}에 대해 매칭을 시도하는 메서드.
	 *
	 * <ol>
	 *   <li>ES에서 해당 유저의 매칭 조건 문서를 조회</li>
	 *   <li>조건에 맞는 상대 유저 목록을 Elasticsearch DSL 쿼리로 검색</li>
	 *   <li>목록이 비어 있으면 재시도 큐로 이동</li>
	 *   <li>목록이 존재하면 가장 높은 점수의 유저를 선택하여:
	 *       <ul>
	 *         <li>ES 문서를 양쪽 모두 삭제</li>
	 *         <li>Redis에 매칭 완료 상태 기록</li>
	 *         <li>재시도 큐에서 제거</li>
	 *       </ul>
	 *   </li>
	 * </ol>
	 *
	 * @param memberId 매칭을 시도할 회원 ID
	 */
	public void tryMatching(Long memberId) {
		try {
			//es에서 매칭을 시도할 유저의 document를 가져온다.
			MatchingUserDocument matchingUserDocument = elasticSearchService.getDocByMemberId(memberId);

			// es에서 매칭 상대 리스트 조회
			List<MatchingUserDocument> matchedUsers = esMatchingUserFinder.findMatchingUsersByScriptScore(matchingUserDocument);

			// 매칭 조건에 맞는 유저가 없을 시 retry Set으로 이동
			if (matchedUsers.isEmpty() || matchedUsers.getFirst().getMatchingScore() < 40) {
				log.info("[INFO] 매칭 상대 없음 : memberId={}", memberId);
				matchingStateManager.removeUserFromMatchingState(memberId);
				matchingStateManager.addUserToRetryState(memberId);
				return;
			}

			MatchingUserDocument bestMatchingUser = matchedUsers.getFirst(); // 매칭 점수가 가장 높은 유저
			log.info("[INFO] 매칭 성공! (memberId={}) >>>>> (memberID={})", memberId, bestMatchingUser.getMemberId());//TODO 점수 프론트까지 보내주기
			log.info("[INFO] 둘의 매칭 점수: {}", bestMatchingUser.getMatchingScore());
			redisService.addToMatchedUsers(memberId, bestMatchingUser.getMemberId());

			// ES에서 매칭된 유저들의 데이터 삭제
			elasticSearchService.deleteDocByMemberId(memberId);
			elasticSearchService.deleteDocByMemberId(bestMatchingUser.getMemberId());

			matchingStateManager.completeMatchingForBothUsers(memberId, bestMatchingUser.getMemberId());
			matchingStateManager.removeUserFromRetryState(memberId);
			matchingStateManager.removeUserFromRetryState(bestMatchingUser.getMemberId());
		} catch (Exception e) {
			log.error("[ERROR] 매칭 시도 중 에러 발생 : {}", e.getMessage());
		}
	}
}
