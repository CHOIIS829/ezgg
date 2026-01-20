package com.matching.ezgg.domain.matchInfo.matchKeyword.rule.jugRule;

import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.parsingDto.JugMatchParsingDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.keyword.JugKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.lane.Lane;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.KeywordRule;

public class ObjectiveTakerRule implements KeywordRule<JugMatchParsingDto, JugKeyword> {
	@Override
	public Boolean matchWithRule(JugMatchParsingDto jugMatchParsingDto, Lane lane) {
		int objectiveSum = jugMatchParsingDto.getRiftHeraldTakedowns() + jugMatchParsingDto.getBaronTakedowns() + jugMatchParsingDto.getDragonTakedowns();
		int opponentObjectiveSum = jugMatchParsingDto.getOpponentRiftHeraldTakeDowns() + jugMatchParsingDto.getOpponentBaronTakedowns() + jugMatchParsingDto.getOpponentDragonTakedowns();
		return objectiveSum + 2 > opponentObjectiveSum;
	}

	@Override
	public JugKeyword getKeyword() {
		return JugKeyword.OBJECTIVE_TAKER;
	}

	@Override
	public String getDescription() {
		return getKeyword().getDescription();
	}
}
