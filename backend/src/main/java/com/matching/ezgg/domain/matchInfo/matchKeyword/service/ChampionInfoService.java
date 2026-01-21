package com.matching.ezgg.domain.matchInfo.matchKeyword.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.matching.ezgg.domain.matchInfo.matchKeyword.championInfo.ChampionBasicInfo;
import com.matching.ezgg.domain.matchInfo.matchKeyword.championInfo.ChampionRole;
import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.analysis.Analysis;
import com.matching.ezgg.domain.matching.dto.PreferredPartnerParsingDto;
import com.matching.ezgg.global.exception.NullChampionsException;
import com.matching.ezgg.global.exception.PreferredChampionsException;
import com.matching.ezgg.global.exception.UnpreferredChampionsException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChampionInfoService {

	/** 유저가 입력한 매칭 옵션을 임베딩 모델에 넣을 형태로 변환
	 * @param preferredPartnerParsingDto
	 * return 임베딩 모델에 넣을 매칭 옵션 정보가 담긴 JSON 문자열
	 */

	public PreferredPartnerParsingDto mergeChampionPreferenceWithPlayStyleJson(
		PreferredPartnerParsingDto preferredPartnerParsingDto) {
		List<String> preferredChampions = preferredPartnerParsingDto.getChampionInfo().getPreferredChampions();
		List<String> unpreferredChampions = preferredPartnerParsingDto.getChampionInfo().getUnpreferredChampions();
		String playstyle = preferredPartnerParsingDto.getUserPreferenceText();

		if (preferredChampions != null && preferredChampions.size() > 3) {
			throw new PreferredChampionsException();
		}
		if (unpreferredChampions != null && unpreferredChampions.size() > 3) {
			throw new UnpreferredChampionsException();
		}

		if (preferredChampions == null || preferredChampions.isEmpty()) {
			throw new NullChampionsException();
		}
		if (unpreferredChampions == null || unpreferredChampions.isEmpty()) {
			throw new NullChampionsException();
		}

		//챔피언 역할 점수를 저장할 championRoleScore
		Map<String, Integer> championRoleScore = new LinkedHashMap<>();
		for (ChampionRole role : ChampionRole.values()) {
			championRoleScore.put(role.name(), 0);
		}

		//점수 업데이트. 선호 챔피언이면 +1, 비선호 챔피언이면 -1
		updateRoleScore(preferredChampions, championRoleScore, 1);
		updateRoleScore(unpreferredChampions, championRoleScore, -1);

		ObjectNode result = buildChampionPreferenceJson(championRoleScore, playstyle);
		String newUserPreferenceText = toJsonString(result);
		preferredPartnerParsingDto.setUserPreferenceText(newUserPreferenceText);

		return preferredPartnerParsingDto;
	}

	/**
	 * 20경기 데이터를 기반으로 챔피언 역할 등급을 계산하는 메서드
	 * @param championRoleCount
	 * @return 챔피언 역할 등급 String
	 */

	private String evaluateChampionRolePreference(int championRoleCount, int lanePlayCount) {
		if (championRoleCount == 0) {
			return "없음";
		}
		if (lanePlayCount <= 3) { //해당 라인을 3회 이하로 플레이한 경우
			return "보통";
		}
		//20경기 동안 하나의 챔피언 역할을 8회 이상 플레이 했으면 "좋음",아니면 "보통"
		double ratio = (double)championRoleCount / lanePlayCount;
		return ratio >= 0.4 ? "좋음" : "보통";
	}

	/**
	 * 챔피언명에서 특수문자 및 공백을 제거하고 대문자로 바꾸는 메서드
	 * @param championName
	 * @return 챔피언명 String
	 */

	public String cleanedName(String championName) {
		log.info("[INFO] 챔피언 이름: {}", championName);
		return championName
			.replaceAll("[^a-zA-Z0-9]", "")
			.toUpperCase();
	}

	/**
	 * 라인별 챔피언 역할 등급 계산하는 메서드
	 * @param championRoleCounts
	 * @param lanePlayCount
	 * @param analysis
	 */

	public void evaluateChampionRolesForLane(Map<ChampionRole, Integer> championRoleCounts, int lanePlayCount,
		Analysis<? extends Enum<?>> analysis) {
		if (championRoleCounts == null)
			return;

		for (Map.Entry<ChampionRole, Integer> entry : championRoleCounts.entrySet()) {
			ChampionRole role = entry.getKey();
			int count = entry.getValue();
			analysis.getChampionRole().put(role.name(), evaluateChampionRolePreference(count, lanePlayCount));
		}
	}

	private String toJsonString(ObjectNode objectNode) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			String jsonString = objectMapper.writeValueAsString(objectNode);
			// log.info("[INFO] championPreferenceWithPlaystyle: {}", jsonString);
			return jsonString;
		} catch (JsonProcessingException e) {
			log.error("[ERROR] JSON 문자열로 변환 중 JsonProcessingException 발생: {}", e.getMessage());
			return "";
		}
	}

	/**
	 * 챔피언 역할 점수를 계산하는 메서드
	 * @param champions
	 * @param championRoleScore
	 * @param score
	 */

	private void updateRoleScore(List<String> champions, Map<String, Integer> championRoleScore, int score) {
		if (champions != null) {
			for (String champion : champions) {
				List<ChampionRole> championRoles = ChampionBasicInfo.valueOf(cleanedName(champion)).getChampionRoles();
				for (ChampionRole role : championRoles) {
					championRoleScore.put(role.name(), championRoleScore.getOrDefault(role.name(), 0) + score);
				}
			}
		}
	}

	/**
	 * evaluateChampionRolePreference로 챔피언 역할 선호도 점수를 계산하고 mergeWithPlaystyleJson로 JSON을 통합하는 메서드
	 * @param championRole
	 * @param playstyle
	 * @return
	 */

	private ObjectNode buildChampionPreferenceJson(Map<String, Integer> championRole, String playstyle) {
		//챔피언 역할 선호 등급을 저장할 Map 생성
		//모든 챔피언
		Map<String, String> championPreference = new LinkedHashMap<>();
		for (ChampionRole role : ChampionRole.values()) {
			championPreference.put(role.name(), "보통");
		}
		for (Map.Entry<String, Integer> role : championRole.entrySet()) {
			String championRoleName = role.getKey();
			int championRoleCount = role.getValue();
			championPreference.put(championRoleName, evaluateChampionRolePreference(championRoleCount));
		}

		return mergeWithPlaystyleJson(championPreference, playstyle);
	}

	/**
	 * 선호/비선호 챔피언 역할 JSON과 플레이 스타일 JSON을 하나의 JSON으로 합치는 메서드
	 * @param rolePreference
	 * @param playstyleJson
	 * @return
	 */

	private ObjectNode mergeWithPlaystyleJson(Map<String, String> rolePreference, String playstyleJson) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode result = mapper.createObjectNode();
		try {
			ObjectNode playstyle = (ObjectNode)mapper.readTree(playstyleJson);
			result.set("championRole", mapper.valueToTree(rolePreference));
			result.setAll(playstyle);
		} catch (JsonProcessingException e) {
			log.error("[ERROR] JsonProcessingException: {}", e.getMessage());
		}
		return result;
	}

	/**
	 * 사용자 입력 데이터를 기반으로 챔피언 역할 등급을 계산하는 메서드
	 * @param count
	 * @return
	 */

	private String evaluateChampionRolePreference(int count) {
		return count > 0 ? "좋음" : count < 0 ? "싫음" : "보통";
	}
}
