package com.matching.ezgg.domain.recentTwentyMatch.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matching.ezgg.domain.matchInfo.dto.MatchInfoDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.championInfo.ChampionBasicInfo;
import com.matching.ezgg.domain.matchInfo.matchKeyword.championInfo.ChampionRole;
import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.analysis.Analysis;
import com.matching.ezgg.domain.matchInfo.matchKeyword.keyword.JugKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.keyword.LanerKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.keyword.SupKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.lane.Lane;
import com.matching.ezgg.domain.matchInfo.matchKeyword.service.ChampionInfoService;
import com.matching.ezgg.domain.matchInfo.matchKeyword.service.KeywordService;
import com.matching.ezgg.domain.matchInfo.service.MatchInfoService;
import com.matching.ezgg.domain.memberInfo.dto.MemberInfoDto;
import com.matching.ezgg.domain.memberInfo.service.MemberInfoService;
import com.matching.ezgg.domain.recentTwentyMatch.dto.RecentTwentyMatchDto;
import com.matching.ezgg.domain.recentTwentyMatch.entity.model.ChampionStat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecentTwentyMatchBuilderService {

	private final MemberInfoService memberInfoService;
	private final MatchInfoService matchInfoService;
	private final KeywordService keywordService;
	private final ChampionInfoService championInfoService;

	// recentTwentyMatchDto 생성
	public RecentTwentyMatchDto buildDto(String puuid) {
		log.info("recentTwentyMatch 계산 시작");

		MemberInfoDto memberInfoDto = memberInfoService.getMemberInfoByPuuid(puuid);
		List<String> matchIds = memberInfoDto.getMatchIds();
		Long memberId = memberInfoDto.getMemberId();

		// recentTwentyMatch에 들어갈 값 계산(sumKills, sumDeaths, sumAssists, wins, losses, allChampionStat)
		AggregateResult result = calculateAggregateStatsFromMatches(matchIds, memberId);

		// 라인별 keywordAnalysis에 들어갈 값 계산
		MatchAnalysisResult matchAnalysisResult = makeKeywordJson(result);

		// 20경기 승률 계산
		int winRate = calculateWinRate(result.wins, result.losses);

		// most 3 챔피언들로 championStat 압축
		Map<String, ChampionStat> most3ChampionStats = extractMost3ChampionStats(result.allChampionStats);

		RecentTwentyMatchDto recentTwentyMatchDto = RecentTwentyMatchDto.builder()
			.memberId(memberId)
			.sumKills(result.sumKills)
			.sumDeaths(result.sumDeaths)
			.sumAssists(result.sumAssists)
			.winRate(winRate)
			.championStats(most3ChampionStats)
			.topAnalysis(convertToJson(matchAnalysisResult.topKeywordAnalysis))
			.jugAnalysis(convertToJson(matchAnalysisResult.jugKeywordAnalysis))
			.midAnalysis(convertToJson(matchAnalysisResult.midKeywordAnalysis))
			.adAnalysis(convertToJson(matchAnalysisResult.adKeywordAnalysis))
			.supAnalysis(convertToJson(matchAnalysisResult.supKeywordAnalysis))
			.build();

		log.info("recentTwentyMatch 계산 종료");
		return recentTwentyMatchDto;
	}

	public String convertToJson(Analysis<? extends Enum<?>> analysis) {
		ObjectMapper mapper = new ObjectMapper();
		String json = "";
		try {
			json = mapper.writeValueAsString(analysis);
			log.info("[INFO] Analysis : {}", json);
		} catch (JsonProcessingException e) {
			log.error("[ERROR] Analysis : {}", e.getMessage());
		}
		return json;
	}

	// 계산 결과값 보관하기 위한 내부 클래스
	private static class AggregateResult {
		int sumKills = 0;
		int sumDeaths = 0;
		int sumAssists = 0;
		int wins = 0;
		int losses = 0;
		Map<String, Integer> laneCount = new LinkedHashMap<>();
		Map<String, Integer> topKeywordCount = new LinkedHashMap<>();
		Map<String, Integer> jugKeywordCount = new LinkedHashMap<>();
		Map<String, Integer> midKeywordCount = new LinkedHashMap<>();
		Map<String, Integer> adKeywordCount = new LinkedHashMap<>();
		Map<String, Integer> supKeywordCount = new LinkedHashMap<>();
		Map<ChampionRole, Integer> topRoleCount = new LinkedHashMap<>();
		Map<ChampionRole, Integer> jugRoleCount = new LinkedHashMap<>();
		Map<ChampionRole, Integer> midRoleCount = new LinkedHashMap<>();
		Map<ChampionRole, Integer> adRoleCount = new LinkedHashMap<>();
		Map<ChampionRole, Integer> supRoleCount = new LinkedHashMap<>();
		Map<String, ChampionStat> allChampionStats = new HashMap<>();
	}

	// recentTwentyMatch 계산 메서드
	private AggregateResult calculateAggregateStatsFromMatches(List<String> matchIds, Long memberId) {

		log.info("Riot Api Match 반복문 시작");
		AggregateResult result = new AggregateResult();

		// 최근 20 경기의 matchId들로 RecentTwentyMatch 업데이트
		for (String matchId : matchIds) {
			MatchInfoDto matchInfoDto = matchInfoService.getMemberInfoByMemberIdAndRiotMatchId(memberId, matchId);

			result.sumKills += matchInfoDto.getKills();
			result.sumDeaths += matchInfoDto.getDeaths();
			result.sumAssists += matchInfoDto.getAssists();

			if (Boolean.TRUE.equals(matchInfoDto.getWin())) {
				result.wins++;
			} else {
				result.losses++;
			}

			// championStat 업데이트
			result.allChampionStats
				.computeIfAbsent(matchInfoDto.getChampionName().trim().toLowerCase(),
					name -> ChampionStat.builder()
						.championName(name)
						.build())// 해당 챔피언이 처음으로 기록될때만 ChampionStat 객체 생성. 있을 시에는 해당 championStat 객체에 updateByMatch메서드 바로 적용
				.updateByMatch(matchInfoDto);

			// 라인별 KeywordAnalysis에 사용할 요소들 업데이트
			Lane lane = null;

			try {
				lane = Lane.valueOf(matchInfoDto.getTeamPosition());
			} catch (IllegalArgumentException | NullPointerException e) {
				throw new IllegalArgumentException("유효하지 않은 Lane명 입니다.", e);
			}

			// 라인 횟수 카운트
			result.laneCount.put(lane.name(), result.laneCount.getOrDefault(lane.name(), 0) + 1);

			// 라인별 키워드 횟수 카운트
			Map<Lane, Map<String, Integer>> laneKeywordMap = createLaneKeywordMap(result);

			//특정 라인의 키워드 카운트 맵 꺼냄
			Map<String, Integer> keywordCountMap = laneKeywordMap.get(lane);

			for (String keyword : matchInfoDto.getMatchKeywords()) {
				keywordCountMap.put(keyword, keywordCountMap.getOrDefault(keyword, 0) + 1);
			}

			// 라인별 챔피언 역할 횟수 카운트
			Map<Lane, Map<ChampionRole, Integer>> laneChampionRoleMap = createChampionRoleMap(result);

			//특정 라인의 챔피언 롤 카운트 맵 꺼냄
			Map<ChampionRole, Integer> championRoleCountMap = laneChampionRoleMap.get(lane);

			String championName = championInfoService.cleanedName(matchInfoDto.getChampionName());
			List<ChampionRole> championRoles = new ArrayList<>();
			try {
				championRoles = ChampionBasicInfo.valueOf(championName).getChampionRoles();
			} catch (IllegalArgumentException | NullPointerException e) {
				throw new IllegalArgumentException("유효하지 않은 챔피언명 입니다.", e);
			}

			for (ChampionRole championRole : championRoles) {
				championRoleCountMap.put(championRole, championRoleCountMap.getOrDefault(championRole, 0) + 1);
			}
		}

		return result;
	}


	/**
	 * 각 라인별 MatchAnalysis 보관용 내부 클래스
	 */

	private static class MatchAnalysisResult {
		Analysis<LanerKeyword> topKeywordAnalysis = Analysis.<LanerKeyword>builder()
			.enumClass(LanerKeyword.class)
			.build();
		Analysis<JugKeyword> jugKeywordAnalysis = Analysis.<JugKeyword>builder()
			.enumClass(JugKeyword.class)
			.build();
		Analysis<LanerKeyword> midKeywordAnalysis = Analysis.<LanerKeyword>builder()
			.enumClass(LanerKeyword.class)
			.build();
		Analysis<LanerKeyword> adKeywordAnalysis = Analysis.<LanerKeyword>builder()
			.enumClass(LanerKeyword.class)
			.build();
		Analysis<SupKeyword> supKeywordAnalysis = Analysis.<SupKeyword>builder()
			.enumClass(SupKeyword.class)
			.build();
	}

	/**
	 * 라인과 라인별 키워드 횟수를 묶은 Map을 생성하는 메서드
	 * @param result
	 * @return Map<Lane, Map<String(키워드명), Integer(횟수)>>
	 */

	private Map<Lane, Map<String, Integer>> createLaneKeywordMap(AggregateResult result) {
		return Map.of(
			Lane.TOP, result.topKeywordCount,
			Lane.JUNGLE, result.jugKeywordCount,
			Lane.MIDDLE, result.midKeywordCount,
			Lane.BOTTOM, result.adKeywordCount,
			Lane.UTILITY, result.supKeywordCount
		);
	}

	/**
	 * 라인과 championRole 키워드 횟수를 묶은 Map을 생성하는 메서드
	 * @param result
	 * @return Map<Lane, Map<String(챔피언 역할명), Integer(횟수)>>
	 */

	private Map<Lane, Map<ChampionRole, Integer>> createChampionRoleMap(AggregateResult result) {
		return Map.of(
			Lane.TOP, result.topRoleCount,
			Lane.JUNGLE, result.jugRoleCount,
			Lane.MIDDLE, result.midRoleCount,
			Lane.BOTTOM, result.adRoleCount,
			Lane.UTILITY, result.supRoleCount
		);
	}

	/**
	 * 라인에 따른 Analysis를 반환하는 메서드
	 * @param result
	 * @param lane
	 * @return
	 */
	private Analysis<? extends Enum<?>> getAnalysisByLane(MatchAnalysisResult result, Lane lane) {
		return switch (lane) {
			case TOP -> result.topKeywordAnalysis;
			case JUNGLE -> result.jugKeywordAnalysis;
			case MIDDLE -> result.midKeywordAnalysis;
			case BOTTOM -> result.adKeywordAnalysis;
			case UTILITY -> result.supKeywordAnalysis;
		};
	}

	/**
	 * 키워드 등급과 챔피언 역할 등급을 계산하고
	 * 해당 등급들을 Analysis 클래스를 이용해 JSON 형태로 만드는 메서드
	 * @param result
	 * @return 각 라인별 키워드 분석 Analysis가 들어있는 keywordAnalysisResult
	 */

	private MatchAnalysisResult makeKeywordJson(AggregateResult result) {
		Map<String, Integer> laneCounts = result.laneCount;

		//기본 Analysis 생성
		MatchAnalysisResult matchAnalysisResult = new MatchAnalysisResult();

		//라인과 라인별 키워드 횟수를 묶은 Map 생성
		Map<Lane, Map<String, Integer>> laneKeywordMap = createLaneKeywordMap(result);

		//라인과 챔피언 롤 횟수를 묶은 Map 생성
		Map<Lane, Map<ChampionRole, Integer>> championRoleMap = createChampionRoleMap(result);

		//laneKeywordMap 돌며 반복
		for (Map.Entry<Lane, Map<String, Integer>> entry : laneKeywordMap.entrySet()) {

			Lane lane = entry.getKey();
			Map<String, Integer> keywordCounts = entry.getValue();
			int lanePlayCount = laneCounts.getOrDefault(lane.name(), 0);

			//라인에 따라 사용할 Analysis 변경
			Analysis<? extends Enum<?>> analysis = getAnalysisByLane(matchAnalysisResult, lane);

			//라인별 키워드 등급 계산
			keywordService.evaluateKeywordsForLane(keywordCounts, lanePlayCount, analysis);
		}

		//championRoleMap 돌며 반복
		for (Map.Entry<Lane, Map<ChampionRole, Integer>> entry : championRoleMap.entrySet()) {

			Lane lane = entry.getKey();
			Map<ChampionRole, Integer> championRoleCounts = entry.getValue();
			int lanePlayCount = laneCounts.getOrDefault(lane.name(), 0);

			//라인에 따라 사용할 Analysis 변경
			Analysis<? extends Enum<?>> analysis = getAnalysisByLane(matchAnalysisResult, lane);

			//라인별 챔피언 역할 등급 계산
			championInfoService.evaluateChampionRolesForLane(championRoleCounts, lanePlayCount, analysis);

		}
		return matchAnalysisResult;
	}


	// 20경기 승률 계산 메서드
	private int calculateWinRate(int wins, int losses) {
		int total = wins + losses;
		return total == 0 ? 0 : (wins * 100) / total;
	}

	// 모스트 3 챔피언 계산 메서드
	private Map<String, ChampionStat> extractMost3ChampionStats(Map<String, ChampionStat> allChampionStats) {

		// most 3를 정할때, 20경기 중 동일한 판수를 가진 챔피언이 있으면, kda가 높은순으로 선택
		return allChampionStats.values().stream()
			.sorted(Comparator
				.comparingInt(ChampionStat::getTotal).reversed()
				.thenComparing(Comparator.comparingDouble(ChampionStat::calculateKda)
					.reversed())
			)
			.limit(3)
			.collect(Collectors.toMap(
				ChampionStat::getChampionName, // key 값
				Function.identity(), // value 값(championStat)
				(a, b) -> a, // key 중복 발생 시 첫번째를 선택
				LinkedHashMap::new // 모스트 1,2,3 순서가 유지되도록 LinkedHashMap 사용
			));
	}
}
