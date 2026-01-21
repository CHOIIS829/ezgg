package com.matching.ezgg.domain.matchInfo.matchKeyword.rule.jugRule;

import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.parsingDto.JugMatchParsingDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.keyword.JugKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.lane.Lane;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.KeywordRule;

public class JugVisionDominaceRule implements KeywordRule<JugMatchParsingDto, JugKeyword> {
	@Override
	public Boolean matchWithRule(JugMatchParsingDto jugMatchParsingDto, Lane lane) {
		return jugMatchParsingDto.getVisionScoreAdvantageLaneOpponent() >= 0.5;
	}

	@Override
	public JugKeyword getKeyword() {
		return JugKeyword.VISION_DOMINANCE;
	}

	@Override
	public String getDescription() {
		return getKeyword().getDescription();
	}
}
