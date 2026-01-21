package com.matching.ezgg.domain.riotApi.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.matching.ezgg.domain.matchInfo.matchKeyword.service.KeywordAnalyzerService;
import com.matching.ezgg.domain.riotApi.dto.MatchDto;
import com.matching.ezgg.domain.riotApi.dto.PuuidDto;
import com.matching.ezgg.domain.riotApi.dto.WinRateNTierDto;
import com.matching.ezgg.domain.riotApi.util.MatchMapper;
import com.matching.ezgg.global.exception.RiotAccountNotFoundException;
import com.matching.ezgg.global.exception.RiotApiException;
import com.matching.ezgg.global.exception.RiotMatchIdsNotFoundException;
import com.matching.ezgg.global.exception.RiotMatchNotFoundException;
import com.matching.ezgg.global.exception.RiotTierNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ApiService {

	@Qualifier("asia")
	private final RestTemplate asiaRestTemplate;
	@Qualifier("kr")
	private final RestTemplate krRestTemplate;
	private final String apiKey;
	private final MatchMapper matchMapper;
	private final KeywordAnalyzerService keywordAnalyzerService;

	public ApiService(@Qualifier("asia") RestTemplate asiaRestTemplate, @Qualifier("kr") RestTemplate krRestTemplate,
		@Value("${api.key}") String apiKey, MatchMapper matchMapper, KeywordAnalyzerService keywordAnalyzerService) {
		this.asiaRestTemplate = asiaRestTemplate;
		this.krRestTemplate = krRestTemplate;
		this.apiKey = apiKey;
		this.matchMapper = matchMapper;
		this.keywordAnalyzerService = keywordAnalyzerService;
	}

	// riot/account/v1/accounts/by-riot-id/{riot-id}/{tag}?api_key=
	// 유저 id, tag -> Riot api -> puuid를 수령
	public String getMemberPuuid(String riotId, String tag) {
		log.info("[INFO] puuid 조회 시작: {}#{}", riotId, tag);

		try {
			String url = String.format(
				"/riot/account/v1/accounts/by-riot-id/%s/%s?api_key=%s",
				riotId, tag, apiKey
			);

			PuuidDto dto = asiaRestTemplate.getForObject(url, PuuidDto.class);

			if (dto == null || dto.getPuuid() == null) {
				throw new RiotAccountNotFoundException(riotId, tag);
			}

			log.info("[INFO] puuid 조회 성공: {}#{}", riotId, tag);
			return dto.getPuuid();

		} catch (HttpClientErrorException.NotFound e) {
			throw new RiotAccountNotFoundException(riotId, tag);
		} catch (RestClientException e) {
			throw new RiotApiException("puuid 조회 Riot Api 실패");
		}
	}

	// lol/league/v4/entries/by-puuid/{encryptedPUUID}
	// puuid -> Riot api -> tier, rank, wins, losses 수령
	public WinRateNTierDto getMemberWinRateNTier(String puuid) {
		log.info("[INFO] 승률/티어 조회 시작: {}", puuid);

		try {
			String url = String.format(
				"/lol/league/v4/entries/by-puuid/%s?api_key=%s",
				puuid, apiKey
			);

			WinRateNTierDto[] dtoArr = krRestTemplate.getForObject(url, WinRateNTierDto[].class);

			if (dtoArr == null) {
				throw new RiotTierNotFoundException(puuid);
			}

			if (dtoArr.length == 0) {
				return WinRateNTierDto.unranked(puuid);
			}

			// Riot API에서 배열 구조로 게임 큐타입 단위로 티어/승률 객체 전송 -> 개인/2인 랭크 큐타입 데이터만 저장
			WinRateNTierDto winRateNTierDto = Arrays.stream(dtoArr)
				.filter(dto -> "RANKED_SOLO_5x5".equalsIgnoreCase(dto.getQueueType()))
				.findFirst()
				.orElse(WinRateNTierDto.unranked(puuid));

			log.info("[INFO] 승률/티어 조회 성공: {}", puuid);
			return winRateNTierDto;

		} catch (HttpClientErrorException.NotFound e) {
			throw new RiotTierNotFoundException(puuid);
		} catch (RestClientException e) {
			throw new RiotApiException("승률/티어 조회 Riot Api 실패");
		}
	}

	// lol/match/v5/matches/by-puuid/{puuid}/ids?type=ranked&start=0&count=20&api_key=apiKey
	// puuid -> Riot api -> 최근 랭크 경기 20개의 matchIds 배열로 수령
	public List<String> getMemberMatchIds(String puuid) {
		log.info("[INFO] MatchIds 조회 시작: {}", puuid);

		try {
			String url = String.format(
				"/lol/match/v5/matches/by-puuid/%s/ids?type=ranked&start=0&count=20&api_key=%s",
				puuid, apiKey
			);

			List<String> matchIdList = asiaRestTemplate.exchange(
				url,
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<String>>() {
				}
			).getBody();

			if (matchIdList == null) {
				throw new RiotMatchIdsNotFoundException(puuid);
			}

			if (matchIdList.isEmpty()) {
				log.info("[INFO] ranked MatchIds 없음: {}", puuid);
				return matchIdList;
			}

			log.info("[INFO] MatchIds 조회 성공: {}", puuid);
			return matchIdList;

		} catch (RiotMatchIdsNotFoundException e) {
			throw new RiotMatchIdsNotFoundException(puuid);
		} catch (RestClientException e) {
			throw new RiotApiException("MatchIds 조회 Riot Api 실패");
		}
	}

	// lol/match/v5/matches/{matchId}?api_key=
	public MatchDto getMemberMatch(Long memberId, String puuid, String matchId) {
		log.info("[INFO] matchInfo 조회 시작: puuid = {} / matchId = {}", puuid, matchId);

		try {
			String url = String.format(
				"/lol/match/v5/matches/%s?api_key=%s",
				matchId, apiKey
			);
			// Json 전체 수령
			String rawJson = asiaRestTemplate.getForObject(url, String.class);

			// MatchDto 형식으로 매핑
			MatchDto matchDto = matchMapper.toMatchDto(rawJson, memberId, puuid);
			List<String> matchKeywords = keywordAnalyzerService.giveMatchKeyword(rawJson, matchDto.getTeamPosition(),
				puuid, matchId,
				memberId);

			matchDto = matchDto.toBuilder()
				.matchKeywords(matchKeywords)
				.build();

			log.info("[INFO] matchInfo 조회 성공: puuid = {} matchId = {}", puuid, matchId);
			return matchDto;

		} catch (RiotMatchNotFoundException e) {
			throw new RiotMatchNotFoundException(matchId);
		} catch (RiotApiException e) {
			throw new RiotApiException("Match 조회 Riot Api 실패");
		}
	}

	// lol/match/v5/matches/{matchId}/timeline?api_key=
	public JsonNode getDuoMatchTimeline(String matchId) {
		try {
			String url = String.format(
				"/lol/match/v5/matches/%s/timeline?api_key=%s",
				matchId, apiKey
			);

			return asiaRestTemplate.getForObject(url, JsonNode.class);
		} catch (RiotMatchNotFoundException e) {
			throw new RiotMatchNotFoundException(matchId);
		} catch (RiotApiException e) {
			throw new RiotApiException("Match timeline 조회 Riot Api 실패 : MatchId : " + matchId);
		}
	}

	public String getMatch(String matchId) {
		log.info("[INFO] matchInfo 조회 시작: matchId = {}", matchId);
		try {
			String url = String.format(
				"/lol/match/v5/matches/%s?api_key=%s",
				matchId, apiKey
			);

			// Json 전체 수령
			String rawJson = asiaRestTemplate.getForObject(url, String.class);

			log.info("[INFO] matchInfo 조회 성공: matchId = {}", matchId);
			return rawJson;

		} catch (RiotMatchNotFoundException e) {
			throw new RiotMatchNotFoundException(matchId);
		} catch (RiotApiException e) {
			throw new RiotApiException("Match 조회 Riot Api 실패");
		}
	}

}
