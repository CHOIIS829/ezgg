package com.matching.ezgg.domain.matchInfo.matchKeyword.rule.globalRule;

import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.parsingDto.GlobalMatchParsingDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.keyword.GlobalKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.lane.Lane;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.KeywordRule;

public class SurvivabilityRule implements KeywordRule<GlobalMatchParsingDto, GlobalKeyword> {
	@Override
	public Boolean matchWithRule(GlobalMatchParsingDto globalMatchParsingDto, Lane lane) {
		return globalMatchParsingDto.getLongestTimeSpentLiving() > (globalMatchParsingDto.getGameDuration()/3.0) && globalMatchParsingDto.getKda() >= 4.0;
	}

	@Override
	public GlobalKeyword getKeyword() {
		return GlobalKeyword.SURVIVABILITY;
	}

	@Override
	public String getDescription() {
		return getKeyword().getDescription();
	}
}
