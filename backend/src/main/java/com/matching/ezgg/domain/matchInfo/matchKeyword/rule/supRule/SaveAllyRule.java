package com.matching.ezgg.domain.matchInfo.matchKeyword.rule.supRule;

import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.parsingDto.SupMatchParsingDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.keyword.SupKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.lane.Lane;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.KeywordRule;

public class SaveAllyRule implements KeywordRule<SupMatchParsingDto, SupKeyword> {
	@Override
	public Boolean matchWithRule(SupMatchParsingDto supMatchParsingDto, Lane lane) {
		return supMatchParsingDto.getSaveAllyFromDeath()>=1;
	}

	@Override
	public SupKeyword getKeyword() {
		return SupKeyword.SAVE_ALLY;
	}

	@Override
	public String getDescription() {
		return getKeyword().getDescription();
	}
}
