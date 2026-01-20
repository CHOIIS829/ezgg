package com.matching.ezgg.domain.matchInfo.dto;

import com.matching.ezgg.domain.matchInfo.entity.MatchInfo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TimelineMatchInfoDto {
	private Long memberId;
	private String riotMatchId;
	private String teamPosition;
	private String championName;

	public static TimelineMatchInfoDto toDto(MatchInfo matchInfo) {
		return TimelineMatchInfoDto.builder()
			.memberId(matchInfo.getMemberId())
			.riotMatchId(matchInfo.getRiotMatchId())
			.teamPosition(matchInfo.getTeamPosition())
			.championName(matchInfo.getChampionName())
			.build();
	}
}
