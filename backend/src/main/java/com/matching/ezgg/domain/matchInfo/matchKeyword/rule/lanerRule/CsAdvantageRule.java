package com.matching.ezgg.domain.matchInfo.matchKeyword.rule.lanerRule;

import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.parsingDto.LanerMatchParsingDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.keyword.LanerKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.lane.Lane;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.KeywordRule;

public class CsAdvantageRule implements KeywordRule<LanerMatchParsingDto, LanerKeyword> {
	@Override
	public Boolean matchWithRule(LanerMatchParsingDto lanerMatchParsingDto, Lane lane) {
		return lanerMatchParsingDto.getMaxCsAdvantageOnLaneOpponent() >= 20.0;
	}

	@Override
	public LanerKeyword getKeyword() {
		return LanerKeyword.CS_ADVANTAGE;
	}

	@Override
	public String getDescription() {
		return getKeyword().getDescription();
	}
}
