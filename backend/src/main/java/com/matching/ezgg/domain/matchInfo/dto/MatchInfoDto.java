package com.matching.ezgg.domain.matchInfo.dto;

import java.util.ArrayList;
import java.util.List;

import com.matching.ezgg.domain.matchInfo.entity.MatchInfo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MatchInfoDto {
	private Long memberId;
	private String riotMatchId;
	private Integer kills;
	private Integer deaths;
	private Integer assists;
	private String teamPosition;
	private String championName;
	private Boolean win;
	private List<String> matchKeywords = new ArrayList<>();

	public static MatchInfoDto toMatchInfoDto(MatchInfo matchInfo) {
		return MatchInfoDto.builder()
			.memberId(matchInfo.getId())
			.riotMatchId(matchInfo.getRiotMatchId())
			.kills(matchInfo.getKills())
			.deaths(matchInfo.getDeaths())
			.assists(matchInfo.getAssists())
			.teamPosition(matchInfo.getTeamPosition())
			.championName(matchInfo.getChampionName())
			.win(matchInfo.getWin())
			.matchKeywords(matchInfo.getMatchKeywords())
			.build();
	}
}
