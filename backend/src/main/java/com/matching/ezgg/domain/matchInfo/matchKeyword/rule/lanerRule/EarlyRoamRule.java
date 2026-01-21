package com.matching.ezgg.domain.matchInfo.matchKeyword.rule.lanerRule;

import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.parsingDto.LanerMatchParsingDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.keyword.LanerKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.lane.Lane;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.KeywordRule;

public class EarlyRoamRule implements KeywordRule<LanerMatchParsingDto, LanerKeyword> {
	@Override
	public Boolean matchWithRule(LanerMatchParsingDto lanerMatchParsingDto, Lane lane) {
		return lanerMatchParsingDto.getKillsOnOtherLanesEarlyJungleAsLaner() >= 1 || lanerMatchParsingDto.getGetTakedownsInAllLanesEarlyJungleAsLaner() >= 1;
	}

	@Override
	public LanerKeyword getKeyword() {
		return LanerKeyword.EARLY_ROAM;
	}

	@Override
	public String getDescription() {
		return getKeyword().getDescription();
	}
}
