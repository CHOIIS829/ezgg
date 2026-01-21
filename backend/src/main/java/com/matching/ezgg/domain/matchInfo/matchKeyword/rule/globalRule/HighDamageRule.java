package com.matching.ezgg.domain.matchInfo.matchKeyword.rule.globalRule;

import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.parsingDto.GlobalMatchParsingDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.keyword.GlobalKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.lane.Lane;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.KeywordRule;

public class HighDamageRule implements KeywordRule<GlobalMatchParsingDto, GlobalKeyword> {
	@Override
	public Boolean matchWithRule(GlobalMatchParsingDto globalMatchParsingDto, Lane lane) {
		Lane.Criteria criteria = lane.getCriteria();
		return criteria!=null && globalMatchParsingDto.getDamagePerMinute() >= criteria.damagePerMinute();
	}

	@Override
	public GlobalKeyword getKeyword() {
		return GlobalKeyword.HIGH_DAMAGE;
	}

	@Override
	public String getDescription() {
		return getKeyword().getDescription();
	}
}
