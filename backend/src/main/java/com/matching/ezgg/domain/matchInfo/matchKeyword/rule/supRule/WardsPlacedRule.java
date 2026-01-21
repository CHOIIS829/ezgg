package com.matching.ezgg.domain.matchInfo.matchKeyword.rule.supRule;

import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.parsingDto.SupMatchParsingDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.keyword.SupKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.lane.Lane;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.KeywordRule;

public class WardsPlacedRule implements KeywordRule<SupMatchParsingDto,SupKeyword> {
	@Override
	public Boolean matchWithRule(SupMatchParsingDto supMatchParsingDto, Lane lane) {
		return (supMatchParsingDto.getWardPlaced()/(supMatchParsingDto.getGameDuration()/60.0))>=1.2;
	}

	@Override
	public SupKeyword getKeyword() {
		return SupKeyword.WARDS_PLACED;
	}

	@Override
	public String getDescription() {
		return getKeyword().getDescription();
	}
}
