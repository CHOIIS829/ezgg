package com.matching.ezgg.domain.matchInfo.matchKeyword.rule.lanerRule;

import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.parsingDto.LanerMatchParsingDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.keyword.LanerKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.lane.Lane;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.KeywordRule;

public class LaneDominanceRule implements KeywordRule<LanerMatchParsingDto, LanerKeyword> {
	@Override
	public Boolean matchWithRule(LanerMatchParsingDto lanerMatchParsingDto, Lane lane) {
		return lanerMatchParsingDto.getTurretPlatesTaken() +2 >= lanerMatchParsingDto.getOpponentTurretPlatesTaken();
	}

	@Override
	public LanerKeyword getKeyword() {
		return LanerKeyword.LANE_DOMINANCE;
	}

	@Override
	public String getDescription() {
		return getKeyword().getDescription();
	}
}
