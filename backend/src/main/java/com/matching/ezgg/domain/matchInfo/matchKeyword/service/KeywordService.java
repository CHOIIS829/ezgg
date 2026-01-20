package com.matching.ezgg.domain.matchInfo.matchKeyword.service;

import java.util.Arrays;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.MatchKeywordDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.analysis.Analysis;
import com.matching.ezgg.domain.matchInfo.matchKeyword.entity.MatchKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.keyword.GlobalKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.lane.Lane;
import com.matching.ezgg.domain.matchInfo.matchKeyword.repository.MatchKeywordRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeywordService {

	private final MatchKeywordRepository matchKeywordRepository;

	@Transactional
	public MatchKeyword createMatchKeyword(String keywordDescription, Lane lane, String matchId, Long memberId) {
		log.info("[INFO] MatchKeyword 생성 완료: {}, {}", lane, keywordDescription);
		return MatchKeyword.builder()
			.memberId(memberId)
			.keyword(keywordDescription)
			.lane(lane)
			.riotMatchId(matchId)
			.build();
	}

	@Transactional
	public void saveMatchKeyword(MatchKeyword matchKeyword) {
		MatchKeyword savedMatchKeyword = matchKeywordRepository.save(matchKeyword);
		MatchKeywordDto matchKeywordDto = MatchKeywordDto.toDto(savedMatchKeyword);
	}

	/**
	 * 경기 수 대비 키워드 수로 키워드 등급 계산하는 메서드
	 * @param keywordCount
	 * @param lanePlayCount
	 * @return 키워드 등급 String
	 */

	private String evaluateKeyword(int keywordCount, int lanePlayCount) {
		if (lanePlayCount <= 3) { //해당 라인을 3회 이하로 플레이한 경우
			return (keywordCount >= 1) ? "평범" : "없음"; //평범 아니면 없음만 리턴
		}

		if (keywordCount == 0) {
			return "없음";
		}

		double ratio = (double) keywordCount / lanePlayCount;

		if (ratio >= 0.75) return "매우 좋음";
		if (ratio >= 0.5) return "좋음";
		if (ratio >= 0.25) return "평범";
		return "없음";
	}

	private boolean isGlobalKeyword(String keyword) {
		return Arrays.stream(GlobalKeyword.values()).anyMatch(k -> k.name().equalsIgnoreCase(keyword));
	}

	/**
	 * 라인별로 키워드 등급을 계산하여 analysis에 저장
	 * @param keywordCounts
	 * @param lanePlayCount
	 * @param analysis
	 */

	public void evaluateKeywordsForLane(Map<String, Integer> keywordCounts, int lanePlayCount, Analysis<? extends Enum<?>> analysis) {
		if (keywordCounts == null) return;

		for (Map.Entry<String, Integer> entry : keywordCounts.entrySet()) {
			String keyword = entry.getKey();
			int count = entry.getValue();

			if (isGlobalKeyword(keyword)) {
				analysis.getGlobal().put(keyword, evaluateKeyword(count, lanePlayCount));
			} else {
				analysis.getLaner().put(keyword, evaluateKeyword(count, lanePlayCount));
			}
		}
	}
}