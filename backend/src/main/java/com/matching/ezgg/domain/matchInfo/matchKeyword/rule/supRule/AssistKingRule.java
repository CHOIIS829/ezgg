package com.matching.ezgg.domain.matchInfo.matchKeyword.rule.supRule;

import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.parsingDto.SupMatchParsingDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.keyword.SupKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.lane.Lane;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.KeywordRule;

public class AssistKingRule implements KeywordRule<SupMatchParsingDto, SupKeyword> {
	@Override
	public Boolean matchWithRule(SupMatchParsingDto supMatchParsingDto, Lane lane) {
		return (supMatchParsingDto.getAssists()/(supMatchParsingDto.getGameDuration()/60.0))>=0.7;
	}

	@Override
	public SupKeyword getKeyword() {
		return SupKeyword.ASSIST_KING;
	}

	@Override
	public String getDescription() {
		return getKeyword().getDescription();
	}
}
