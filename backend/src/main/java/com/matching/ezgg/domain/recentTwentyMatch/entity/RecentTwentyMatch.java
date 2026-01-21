package com.matching.ezgg.domain.recentTwentyMatch.entity;

import java.util.Map;

import com.matching.ezgg.domain.recentTwentyMatch.entity.model.ChampionStat;
import com.matching.ezgg.domain.recentTwentyMatch.util.ChampionStatsConvert;
import com.matching.ezgg.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "recent_twenty_match")
public class RecentTwentyMatch extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "member_id", unique = true, nullable = false)
	private Long memberId;

	@Column(name = "sum_kills", unique = false, nullable = true)
	private Integer sumKills;

	@Column(name = "sum_deaths", unique = false, nullable = true)
	private Integer sumDeaths;

	@Column(name = "sum_assists", unique = false, nullable = true)
	private Integer sumAssists;

	@Column(name = "champion_stats", unique = false, nullable = true, length = 1000)//TODO 정규화 필요
	@Convert(converter = ChampionStatsConvert.class)
	private Map<String, ChampionStat> championStats;

	@Column(name = "win_rate", unique = false, nullable = true)
	private Integer winRate;

	@Column(name = "top_analysis", unique = false, nullable = true, length=3000)
	private String topAnalysis;

	@Column(name = "jug_analysis", unique = false, nullable = true, length=3000)
	private String jugAnalysis;

	@Column(name = "mid_analysis", unique = false, nullable = true, length=3000)
	private String midAnalysis;

	@Column(name = "ad_analysis", unique = false, nullable = true, length=3000)
	private String adAnalysis;

	@Column(name = "sup_analysis", unique = false, nullable = true, length=3000)
	private String supAnalysis;





	@Builder
	public RecentTwentyMatch(Long memberId, Integer sumKills, Integer sumDeaths, Integer sumAssists,
		Map<String, ChampionStat> championStats, Integer winRate, String topAnalysis, String jugAnalysis, String midAnalysis, String adAnalysis, String supAnalysis) {
		this.memberId = memberId;
		this.sumKills = sumKills;
		this.sumDeaths = sumDeaths;
		this.sumAssists = sumAssists;
		this.championStats = championStats;
		this.winRate = winRate;
		this.topAnalysis = topAnalysis;
		this.jugAnalysis = jugAnalysis;
		this.midAnalysis = midAnalysis;
		this.adAnalysis = adAnalysis;
		this.supAnalysis = supAnalysis;
	}

	public void update(Integer sumKills, Integer sumDeaths, Integer sumAssists,
		Map<String, ChampionStat> championStats, Integer winRate, String topAnalysis, String jugAnalysis, String midAnalysis, String adAnalysis, String supAnalysis) {
		this.sumKills = sumKills;
		this.sumDeaths = sumDeaths;
		this.sumAssists = sumAssists;
		this.championStats = championStats;
		this.winRate = winRate;
		this.topAnalysis = topAnalysis;
		this.jugAnalysis = jugAnalysis;
		this.midAnalysis = midAnalysis;
		this.adAnalysis = adAnalysis;
		this.supAnalysis = supAnalysis;
	}
}
