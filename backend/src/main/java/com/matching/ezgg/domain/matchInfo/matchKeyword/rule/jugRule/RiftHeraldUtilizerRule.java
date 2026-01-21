package com.matching.ezgg.domain.matchInfo.matchKeyword.rule.jugRule;

import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.parsingDto.JugMatchParsingDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.keyword.JugKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.lane.Lane;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.KeywordRule;

public class RiftHeraldUtilizerRule implements KeywordRule<JugMatchParsingDto, JugKeyword> {
	@Override
	public Boolean matchWithRule(JugMatchParsingDto jugMatchParsingDto, Lane lane) {
		return jugMatchParsingDto.getMultiTurretRiftHeraldCount() >= 2;
	}

	@Override
	public JugKeyword getKeyword() {
		return JugKeyword.RIFT_HERALD_UTILIZER;
	}

	@Override
	public String getDescription() {
		return getKeyword().getDescription();
	}
}
