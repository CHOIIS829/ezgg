package com.matching.ezgg.domain.matchInfo.matchKeyword.rule.jugRule;

import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.parsingDto.JugMatchParsingDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.keyword.JugKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.lane.Lane;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.KeywordRule;

public class JungleDominanceRule implements KeywordRule<JugMatchParsingDto, JugKeyword> {

	@Override
	public Boolean matchWithRule(JugMatchParsingDto jugMatchParsingDto, Lane lane) {
		return jugMatchParsingDto.getMoreEnemyJungleThanOpponent()+10 < jugMatchParsingDto.getOpponentMoreEnemyJungleThanOpponent();
	}

	@Override
	public JugKeyword getKeyword() {
		return JugKeyword.JUNGLE_DOMINANCE;
	}

	@Override
	public String getDescription() {
		return getKeyword().getDescription();
	}
}
