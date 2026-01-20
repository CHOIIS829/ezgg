package com.matching.ezgg.domain.matchInfo.matchKeyword.rule.supRule;

import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.parsingDto.SupMatchParsingDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.keyword.SupKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.lane.Lane;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.KeywordRule;

public class SupVisionDominanceRule implements KeywordRule<SupMatchParsingDto, SupKeyword> {
	@Override
	public Boolean matchWithRule(SupMatchParsingDto supMatchParsingDto, Lane lane) {
		return supMatchParsingDto.getVisionScoreAdvantageLaneOpponent()>=0.5;
	}

	@Override
	public SupKeyword getKeyword() {
		return SupKeyword.VISION_DOMINANCE;
	}

	@Override
	public String getDescription() {
		return getKeyword().getDescription();
	}
}
