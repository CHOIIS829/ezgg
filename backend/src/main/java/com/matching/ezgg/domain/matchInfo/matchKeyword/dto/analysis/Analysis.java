package com.matching.ezgg.domain.matchInfo.matchKeyword.dto.analysis;

import java.util.LinkedHashMap;
import java.util.Map;

import com.matching.ezgg.domain.matchInfo.matchKeyword.championInfo.ChampionRole;
import com.matching.ezgg.domain.matchInfo.matchKeyword.keyword.GlobalKeyword;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * RECENT_TWENTY_MATCH에 라인별 키워드 분석 JSON을 저장하기 위한 클래스
 * @param <E>
 */

@Getter
@RequiredArgsConstructor
public class Analysis<E extends Enum<E>> {
	private final Map<String, String> championRole ;
	private final Map<String, String> global;
	private final Map<String, String> laner;

	@Builder
	public static <E extends Enum<E>> Analysis<E> create(Class<E> enumClass) {
		Map<String, String> championRole = new LinkedHashMap<>();
		Map<String, String> globalAnalysis = new LinkedHashMap<>();
		Map<String, String> lanerAnalysis = new LinkedHashMap<>();

		for (ChampionRole role : ChampionRole.values()) {
			championRole.put(role.name(), "없음");
		}

		for (GlobalKeyword keyword : GlobalKeyword.values()) {
			globalAnalysis.put(keyword.name(), "없음");
		}

		if (enumClass != null && enumClass.getEnumConstants() != null) {
			for (E keyword : enumClass.getEnumConstants()) {
				lanerAnalysis.put(keyword.name(), "없음");
			}
		}
		return new Analysis<>(championRole, globalAnalysis, lanerAnalysis);
	}
}

