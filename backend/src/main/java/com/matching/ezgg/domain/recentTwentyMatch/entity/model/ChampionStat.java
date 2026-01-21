package com.matching.ezgg.domain.recentTwentyMatch.entity.model;

import com.matching.ezgg.domain.matchInfo.dto.MatchInfoDto;
import com.matching.ezgg.domain.matchInfo.entity.MatchInfo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Getter
@NoArgsConstructor
@Slf4j
public class ChampionStat {//모스트 3개 캐릭터에 대해서만 생성

	private String championName;
	private int kills;
	private int deaths;
	private int assists;
	private int wins;
	private int losses;
	private int total;
	private int winRateOfChampion;

	@Builder
	public ChampionStat(String championName, int kills, int deaths, int assists, int wins, int losses) {
		this.championName = championName;
		this.kills = kills;
		this.deaths = deaths;
		this.assists = assists;
		this.wins = wins;
		this.losses = losses;
		recalculate();
	}

	// match 데이터로 championStat 업데이트
	public void updateByMatch(MatchInfoDto matchInfoDto) {
		this.kills += matchInfoDto.getKills();
		this.deaths += matchInfoDto.getDeaths();
		this.assists += matchInfoDto.getAssists();
		addMatchResult(Boolean.TRUE.equals(matchInfoDto.getWin()));
	}

	// 승패 여부 기록
	private void addMatchResult(boolean win) {
		if (win) {
			this.wins++;
		} else {
			this.losses++;
		}
		recalculate();
	}

	// 승률 재계산
	private void recalculate() {
		this.total = wins + losses;
		this.winRateOfChampion = total == 0 ? 0 : (wins * 100) / total;
	}

	// kda 계산
	public double calculateKda() {
		if (deaths == 0) {
			return kills + assists; // 1데스 했다고 가정
		}

		double kda = (double)(kills + assists) / deaths;
		return Math.round(kda * 100.0) / 100.0; // 소수점 2자리 반올림
	}
}
