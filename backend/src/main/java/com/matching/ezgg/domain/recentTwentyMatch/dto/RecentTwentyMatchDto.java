package com.matching.ezgg.domain.recentTwentyMatch.dto;

import java.util.Map;

import com.matching.ezgg.domain.recentTwentyMatch.entity.RecentTwentyMatch;
import com.matching.ezgg.domain.recentTwentyMatch.entity.model.ChampionStat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecentTwentyMatchDto {
	private long memberId;
	private int sumKills;
	private int sumDeaths;
	private int sumAssists;
	private Map<String, ChampionStat> championStats;
	private int winRate;
	private String topAnalysis;
	private String jugAnalysis;
	private String midAnalysis;
	private String adAnalysis;
	private String supAnalysis;

	// 값이 null인경우 기본값 0으로 들어가게 변경
	public static RecentTwentyMatchDto toDto(RecentTwentyMatch recentTwentyMatch) {
		return RecentTwentyMatchDto.builder()
			.memberId(recentTwentyMatch.getMemberId() != null ? recentTwentyMatch.getMemberId() : 0L)
			.sumKills(recentTwentyMatch.getSumKills() != null ? recentTwentyMatch.getSumKills() : 0)
			.sumDeaths(recentTwentyMatch.getSumDeaths() != null ? recentTwentyMatch.getSumDeaths() : 0)
			.sumAssists(recentTwentyMatch.getSumAssists() != null ? recentTwentyMatch.getSumAssists() : 0)
			.championStats(recentTwentyMatch.getChampionStats()) // Map은 null이어도 괜찮음
			.winRate(recentTwentyMatch.getWinRate() != null ? recentTwentyMatch.getWinRate() : 0)
			.topAnalysis(recentTwentyMatch.getTopAnalysis() != null ? recentTwentyMatch.getTopAnalysis() : "")
			.jugAnalysis(recentTwentyMatch.getJugAnalysis() != null ? recentTwentyMatch.getJugAnalysis() : "")
			.midAnalysis(recentTwentyMatch.getMidAnalysis() != null ? recentTwentyMatch.getMidAnalysis() : "")
			.adAnalysis(recentTwentyMatch.getAdAnalysis() != null ? recentTwentyMatch.getAdAnalysis() : "")
			.supAnalysis(recentTwentyMatch.getSupAnalysis() != null ? recentTwentyMatch.getSupAnalysis() : "")
			.build();
	}
}
