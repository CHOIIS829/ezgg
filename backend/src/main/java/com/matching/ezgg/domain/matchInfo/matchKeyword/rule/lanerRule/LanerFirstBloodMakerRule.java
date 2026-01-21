package com.matching.ezgg.domain.matchInfo.matchKeyword.rule.lanerRule;

import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.parsingDto.LanerMatchParsingDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.keyword.LanerKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.lane.Lane;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.KeywordRule;

public class LanerFirstBloodMakerRule implements KeywordRule<LanerMatchParsingDto, LanerKeyword> {
	@Override
	public Boolean matchWithRule(LanerMatchParsingDto lanerMatchParsingDto, Lane lane) {
		return lanerMatchParsingDto.getFirstBloodKill();
	}

	@Override
	public LanerKeyword getKeyword() {
		return LanerKeyword.FIRST_BLOOD_MAKER;
	}

	@Override
	public String getDescription() {
		return getKeyword().getDescription();
	}
}
