package com.matching.ezgg.domain.matchInfo.matchKeyword.analyzer;

import java.util.ArrayList;
import java.util.List;

import com.matching.ezgg.domain.matchInfo.matchKeyword.entity.MatchKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.lane.Lane;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.KeywordRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.service.KeywordService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KeywordAnalyzer<T, K extends Enum<K>> {

	private final KeywordService keywordService;
	private final List<KeywordRule<T, K>> rules;

	public KeywordAnalyzer(KeywordService keywordService, List<KeywordRule<T, K>> rules) {
		this.keywordService = keywordService;
		this.rules = rules;
	}

	/**
	 * matchData를 파싱한 dto를 기준으로 키워드를 생성하고 키워드 리스트를 반환하는 메서드
	 * @param matchParsingDto
	 * @param teamPosition
	 * @param matchId
	 * @param memberId
	 * @return match 키워드 List<String>
	 */

	public List<String> analyze(T matchParsingDto, String teamPosition, String matchId, Long memberId) {

		List<String> keywordList = new ArrayList<>();
		Lane lane;
		try {
			lane = Lane.valueOf(teamPosition);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("유효하지 않은 Lane명 입니다.", e);
		}

		//모든 global 규칙 확인
		for (KeywordRule<T, K> rule : rules) {
			//규칙에 부합하면
			if (rule.matchWithRule(matchParsingDto, lane)) {
				//키워드 생성 및 저장
				String keyword = rule.getKeyword().name();
				MatchKeyword matchKeyword = keywordService.createMatchKeyword(keyword, lane, matchId,
					memberId);
				keywordService.saveMatchKeyword(matchKeyword);
				//키워드 리스트에 추가
				keywordList.add(keyword);
			}
		}
		log.info("[INFO] Analysis: {}, {}", keywordList, memberId);
		return keywordList;
	}

}