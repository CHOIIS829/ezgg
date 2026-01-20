package com.matching.ezgg.domain.matchInfo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.matching.ezgg.domain.matchInfo.dto.MatchInfoDto;
import com.matching.ezgg.domain.matchInfo.dto.TimelineMatchInfoDto;
import com.matching.ezgg.domain.matchInfo.entity.MatchInfo;
import com.matching.ezgg.domain.matchInfo.repository.MatchInfoRepository;
import com.matching.ezgg.domain.riotApi.dto.MatchDto;
import com.matching.ezgg.global.exception.MatchNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchInfoService {
	private final MatchInfoRepository matchInfoRepository;

	@Transactional
	public void save(MatchDto matchDto) {
		log.info("[INFO] match 저장 시작: {}", matchDto.getRiotMatchId());
		MatchInfo matchInfo = MatchInfo.builder()
			.memberId(matchDto.getMemberId())
			.riotMatchId(matchDto.getRiotMatchId())
			.kills(matchDto.getKills())
			.deaths(matchDto.getDeaths())
			.assists(matchDto.getAssists())
			.teamPosition(matchDto.getTeamPosition())
			.championName(matchDto.getChampionName())
			.win(matchDto.isWin())
			.matchKeywords(matchDto.getMatchKeywords())
			.build();

		matchInfoRepository.save(matchInfo);
		log.info("[INFO] match 저장 종료: {}", matchDto.getRiotMatchId());
	}

	private MatchInfo getMatchInfoOrThrow(Long memberId, String matchId) {
		return matchInfoRepository.findByMemberIdAndRiotMatchId(memberId, matchId)
			.orElseThrow(MatchNotFoundException::new);
	}

	public MatchInfoDto getMemberInfoByMemberIdAndRiotMatchId(Long memberId, String matchId) {
		return MatchInfoDto.toMatchInfoDto(getMatchInfoOrThrow(memberId, matchId));
	}

	public TimelineMatchInfoDto getTimelineMemberInfoByMemberIdAndRiotMatchId(Long memberId, String matchId) {
		return TimelineMatchInfoDto.toDto(getMatchInfoOrThrow(memberId, matchId));
	}
}
