package com.matching.ezgg.domain.recentTwentyMatch.util;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.matching.ezgg.domain.recentTwentyMatch.entity.model.ChampionStat;

public class ChampionStatUtils {

	/**
	 * 경기 수(desc) → KDA(desc) 기준으로 상위 3개 챔피언명을 리스트로 반환하는 메서드.
	 */
	public static List<String> orderChampionsByTotalGamesAndKda(Map<String, ChampionStat> statMap) {
		if (statMap == null || statMap.isEmpty()) {
			return List.of();
		}

		return statMap.values().stream()
			// 1. 총 경기 수 내림차순
			.sorted(Comparator.comparingInt(ChampionStat::getTotal).reversed()
				// 2. 경기 수가 같으면 KDA 내림차순
				.thenComparingDouble(ChampionStat::calculateKda).reversed())
			.limit(3)
			.map(ChampionStat::getChampionName)
			.collect(Collectors.toList());
	}
}
