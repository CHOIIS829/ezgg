package com.matching.ezgg.domain.matchInfo.matchKeyword.rule.jugRule;

import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.parsingDto.JugMatchParsingDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.keyword.JugKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.lane.Lane;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.KeywordRule;

public class JugFirstBloodMakerRule implements KeywordRule<JugMatchParsingDto, JugKeyword> {
	@Override
	public Boolean matchWithRule(JugMatchParsingDto jugMatchParsingDto, Lane lane) {
		return jugMatchParsingDto.getFirstBloodKill();
	}

	@Override
	public JugKeyword getKeyword() {
		return JugKeyword.FIRST_BLOOD_MAKER;
	}

	@Override
	public String getDescription() {
		return getKeyword().getDescription();
	}
}
