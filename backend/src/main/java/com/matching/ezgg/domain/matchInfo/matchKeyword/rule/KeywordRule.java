package com.matching.ezgg.domain.matchInfo.matchKeyword.rule;

import com.matching.ezgg.domain.matchInfo.matchKeyword.lane.Lane;

public interface KeywordRule<Dto, Keyword extends Enum<Keyword>> {
	Boolean matchWithRule(Dto parsingDto, Lane lane);
	Keyword getKeyword();
	String getDescription();
}
