package com.matching.ezgg.domain.matchInfo.matchKeyword.dto;

import com.matching.ezgg.domain.matchInfo.matchKeyword.entity.MatchKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.lane.Lane;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MatchKeywordDto {

	private Long memberId;
	private String riotMatchId;
	private String keyword;
	private Lane lane;

	public static MatchKeywordDto toDto(MatchKeyword matchKeyword) {
		return MatchKeywordDto.builder()
			.memberId(matchKeyword.getMemberId())
			.riotMatchId(matchKeyword.getRiotMatchId())
			.keyword(matchKeyword.getKeyword())
			.lane(matchKeyword.getLane())
			.build();
	}

}
