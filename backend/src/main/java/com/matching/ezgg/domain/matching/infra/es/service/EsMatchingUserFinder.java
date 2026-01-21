package com.matching.ezgg.domain.matching.infra.es.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

import com.matching.ezgg.domain.matching.infra.es.index.MatchingUserDocument;
import com.matching.ezgg.global.exception.EsQueryException;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ShardFailure;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.ScriptScoreFunction;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class EsMatchingUserFinder {
	private final ElasticsearchClient esClient;

	public List<MatchingUserDocument> findMatchingUsersByScriptScore(MatchingUserDocument matchingUserDocument) {

		Long myMemberId = matchingUserDocument.getMemberId();
		String tier = matchingUserDocument.getUserProfile().getTier();
		// Double reviewScore = matchingUserDocument.getUserProfile().getReviewScore(); //TODO 리뷰 점수 추후 매칭 점수에 반영

		List<String> most3Champions = matchingUserDocument.getUserProfile()
			.getRecentTwentyMatchStats()
			.getMost3Champions()
			.stream()
			.map(String::toLowerCase)
			.toList();
		float[] topAnalysisVector = matchingUserDocument.getUserProfile()
			.getRecentTwentyMatchStats()
			.getTopAnalysisVector();
		float[] jugAnalysisVector = matchingUserDocument.getUserProfile()
			.getRecentTwentyMatchStats()
			.getJugAnalysisVector();
		float[] midAnalysisVector = matchingUserDocument.getUserProfile()
			.getRecentTwentyMatchStats()
			.getMidAnalysisVector();
		float[] adAnalysisVector = matchingUserDocument.getUserProfile()
			.getRecentTwentyMatchStats()
			.getAdAnalysisVector();
		float[] supAnalysisVector = matchingUserDocument.getUserProfile()
			.getRecentTwentyMatchStats()
			.getSupAnalysisVector();

		float[] userPreferenceTextVector = matchingUserDocument.getPartnerPreference().getUserPreferenceTextVector();
		String myLine = matchingUserDocument.getPartnerPreference().getLineRequirements().getMyLine();
		String partnerLine = matchingUserDocument.getPartnerPreference().getLineRequirements().getPartnerLine();
		List<String> preferredChampions = matchingUserDocument.getPartnerPreference()
			.getChampionsPreference()
			.getPreferredChampions()
			.stream()
			.map(String::toLowerCase)
			.toList();
		List<String> unpreferredChampions = matchingUserDocument.getPartnerPreference()
			.getChampionsPreference()
			.getUnpreferredChampions()
			.stream()
			.map(String::toLowerCase)
			.toList();

		Map<String, JsonData> params = new HashMap<>();

		// 선호/비선호 챔피언
		if (!preferredChampions.isEmpty()) {
			params.put("preferredChampions", JsonData.of(preferredChampions));
		}
		if (!unpreferredChampions.isEmpty()) {
			params.put("unpreferredChampions", JsonData.of(unpreferredChampions));
		}
		// 모스트 3 챔피언
		if (!most3Champions.isEmpty()) {
			params.put("most3Champions", JsonData.of(most3Champions));
		}
		// 키워드 벡터
		if (userPreferenceTextVector != null && userPreferenceTextVector.length == 1536) {
			params.put("keywordSelectionVector", JsonData.of(toFloatList(userPreferenceTextVector)));
		}
		// 라인별 분석 벡터
		if (topAnalysisVector != null && topAnalysisVector.length == 1536)
			params.put("topAnalysisVector", JsonData.of(toFloatList(topAnalysisVector)));
		if (jugAnalysisVector != null && jugAnalysisVector.length == 1536)
			params.put("jugAnalysisVector", JsonData.of(toFloatList(jugAnalysisVector)));
		if (midAnalysisVector != null && midAnalysisVector.length == 1536)
			params.put("midAnalysisVector", JsonData.of(toFloatList(midAnalysisVector)));
		if (adAnalysisVector != null && adAnalysisVector.length == 1536)
			params.put("adAnalysisVector", JsonData.of(toFloatList(adAnalysisVector)));
		if (supAnalysisVector != null && supAnalysisVector.length == 1536)
			params.put("supAnalysisVector", JsonData.of(toFloatList(supAnalysisVector)));

		params.put("myLine", JsonData.of(myLine.toLowerCase()));
		params.put("partnerLine", JsonData.of(partnerLine.toLowerCase()));

		// 2. 검색할 대상을 1차적으로 거르는 필수 조건 Bool Filter
		Query filterQuery = Query.of(bq -> bq
			.bool(b -> b
				.filter(q -> q.term(t -> t.field("userProfile.tier").value(tier)))
				.filter(q -> q.term(t -> t
					.field("partnerPreference.lineRequirements.partnerLine").value(myLine)))
				.filter(q -> q.term(t -> t
					.field("partnerPreference.lineRequirements.myLine").value(partnerLine)))
				.mustNot(mn -> mn.term(t -> t.field("memberId").value(myMemberId)))
			)
		);

		// 3. Painless 스크립트로 챔피언 가중치 계산과 코사인 유사도 계산을 수행하여 score를 누적한 뒤 반환
		ScriptScoreFunction scriptScoreFunction = ScriptScoreFunction.of(sf -> sf
			.script(sc -> sc
				.source("""
					    double s = 0;
					    // 유저1의 챔피언 가중치 계산
					    def pref = params.containsKey('preferredChampions') ? params.preferredChampions : [];
					    def unpref = params.containsKey('unpreferredChampions') ? params.unpreferredChampions : [];
					    if (doc['userProfile.recentTwentyMatchStats.most3Champions'].size() > 0 && pref.size() > 0) {
					      String c1 = doc['userProfile.recentTwentyMatchStats.most3Champions'].get(0);
					      if (pref.contains(c1)) { s += 12; }
					      if (unpref.contains(c1)) { s -= 6; }
					    }
					    if (doc['userProfile.recentTwentyMatchStats.most3Champions'].size() > 1 && pref.size() > 0) {
					      String c2 = doc['userProfile.recentTwentyMatchStats.most3Champions'].get(1);
					      if (pref.contains(c2)) { s += 8; }
					      if (unpref.contains(c2)) { s -= 4; }
					    }
					    if (doc['userProfile.recentTwentyMatchStats.most3Champions'].size() > 2 && pref.size() > 0) {
					      String c3 = doc['userProfile.recentTwentyMatchStats.most3Champions'].get(2);
					      if (pref.contains(c3)) { s += 5; }
					      if (unpref.contains(c3)) { s -= 2; }
					    }
					    // 유저2의 챔피언 가중치 계산
					    def champs = params.containsKey('most3Champions') ? params.most3Champions : [];
					    if (champs.size() > 0) {
					      String c4 = champs.get(0);
					      if (doc['partnerPreference.championsPreference.preferredChampions'].contains(c4)) { s += 12; }
					      if (doc['partnerPreference.championsPreference.unpreferredChampions'].contains(c4)) { s -= 6; }
					    }
					    if (champs.size() > 1) {
					      String c5 = champs.get(1);
					      if (doc['partnerPreference.championsPreference.preferredChampions'].contains(c5)) { s += 8; }
					      if (doc['partnerPreference.championsPreference.unpreferredChampions'].contains(c5)) { s -= 4; }
					    }
					    if (champs.size() > 2) {
					      String c6 = champs.get(2);
					      if (doc['partnerPreference.championsPreference.preferredChampions'].contains(c6)) { s += 5; }
					      if (doc['partnerPreference.championsPreference.unpreferredChampions'].contains(c6)) { s -= 2; }
					    }
					    // 유저 1의 벡터 유사도 계산
					    if (params.containsKey('keywordSelectionVector')) {
						if (params.partnerLine == "top" && doc.containsKey('userProfile.recentTwentyMatchStats.topAnalysisVector')
								&& doc['userProfile.recentTwentyMatchStats.topAnalysisVector'].size() > 0) {
							double cs = cosineSimilarity(params.keywordSelectionVector, 'userProfile.recentTwentyMatchStats.topAnalysisVector');
							s += (cs + 1.0) * 10.0;
						  } else if (params.partnerLine == "jug" && doc.containsKey('userProfile.recentTwentyMatchStats.jugAnalysisVector')
						  		&& doc['userProfile.recentTwentyMatchStats.jugAnalysisVector'].size() > 0) {
							double cs = cosineSimilarity(params.keywordSelectionVector, 'userProfile.recentTwentyMatchStats.jugAnalysisVector');
							s += (cs + 1.0) * 10.0;
						  } else if (params.partnerLine == "mid" && doc.containsKey('userProfile.recentTwentyMatchStats.midAnalysisVector')
						  		&& doc['userProfile.recentTwentyMatchStats.midAnalysisVector'].size() > 0) {
							double cs = cosineSimilarity(params.keywordSelectionVector, 'userProfile.recentTwentyMatchStats.midAnalysisVector');
							s += (cs + 1.0) * 10.0;
						  } else if (params.partnerLine == "ad" && doc.containsKey('userProfile.recentTwentyMatchStats.adAnalysisVector')
						  		&& doc['userProfile.recentTwentyMatchStats.adAnalysisVector'].size() > 0) {
							double cs = cosineSimilarity(params.keywordSelectionVector, 'userProfile.recentTwentyMatchStats.adAnalysisVector');
							s += (cs + 1.0) * 10.0;
						  } else if (params.partnerLine == "sup" && doc.containsKey('userProfile.recentTwentyMatchStats.supAnalysisVector')
						  		&& doc['userProfile.recentTwentyMatchStats.supAnalysisVector'].size() > 0) {
							double cs = cosineSimilarity(params.keywordSelectionVector, doc['userProfile.recentTwentyMatchStats.supAnalysisVector']);
							s += (cs + 1.0) * 12.5;
						  }
					    }
					    // 유저 2의 벡터 유사도 계산
					    if (doc.containsKey('partnerPreference.userPreferenceTextVector') && doc['partnerPreference.userPreferenceTextVector'].size() > 0) {
						if (params.myLine == "top" && params.containsKey('topAnalysisVector')) {
							double cs = cosineSimilarity(params.topAnalysisVector, 'partnerPreference.userPreferenceTextVector');
							s += (cs + 1.0) * 10.0;
						  } else if (params.myLine == "jug" && params.containsKey('jugAnalysisVector')) {
							double cs = cosineSimilarity(params.jugAnalysisVector, 'partnerPreference.userPreferenceTextVector');
							s += (cs + 1.0) * 10.0;
						  } else if (params.myLine == "mid" && params.containsKey('midAnalysisVector')) {
							double cs = cosineSimilarity(params.midAnalysisVector, 'partnerPreference.userPreferenceTextVector');
							s += (cs + 1.0) * 10.0;
						  } else if (params.myLine == "ad" && params.containsKey('adAnalysisVector')) {
							double cs = cosineSimilarity(params.adAnalysisVector, 'partnerPreference.userPreferenceTextVector');
							s += (cs + 1.0) * 10.0;
						  } else if (params.myLine == "sup" && params.containsKey('supAnalysisVector')) {
							double cs = cosineSimilarity(params.supAnalysisVector, 'partnerPreference.userPreferenceTextVector');
							s += (cs + 1.0) * 12.5;
						  }
					    }
					    return Math.max(s,0); // 음수 방지
					"""
				).params(params)
			)
		);

		// 4. 최종 Query 조립
		Query scriptScoreQuery = Query.of(q -> q
			.scriptScore(ss -> ss
				.query(filterQuery)
				.script(scriptScoreFunction.script())
			)
		);

		SearchRequest request = SearchRequest.of(s -> s
			.index("matching-user")
			.size(10) // 상위 10명
			.query(scriptScoreQuery)
		);

		// 5. 실행 및 매핑
		try {
			SearchResponse<MatchingUserDocument> resp = esClient.search(request, MatchingUserDocument.class);

			int failedShards = resp.shards().failed().intValue();
			if (failedShards > 0) {
				for (ShardFailure f : resp.shards().failures()) {
					log.warn("Shard {} failed: {} - {}",
						f.shard(),
						f.reason().type(),
						f.reason().reason());
				}
			}

			return resp.hits().hits().stream()
				.map(hit -> {
					MatchingUserDocument doc = hit.source();
					if (doc != null) {
						doc.setMatchingScore(hit.score() != null ? hit.score().floatValue() : 0);// matchingScore 저장
					}
					return doc;
				})
				.filter(Objects::nonNull)
				.toList();

		}catch (co.elastic.clients.elasticsearch._types.ElasticsearchException e) {
			var err = e.error();// === ErrorResponse( JSON 전체 )

			log.error("ES에 쿼리 실행 중 에러 발생={}", err);
			throw new EsQueryException();
		} catch (IOException e) {
			log.error("[ERROR] Elasticsearch 조건 조회 중 오류 발생: " + e.getMessage());
			throw new EsQueryException();
		}
	}

	// float[] → List<Float> 변환 유틸 메서드
	private List<Float> toFloatList(float[] arr) {
		return IntStream.range(0, arr.length)
			.mapToObj(i -> arr[i])
			.collect(Collectors.toList());
	}

}
