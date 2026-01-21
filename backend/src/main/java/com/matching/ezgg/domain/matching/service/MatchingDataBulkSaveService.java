package com.matching.ezgg.domain.matching.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.matching.ezgg.domain.matchInfo.service.MatchInfoService;
import com.matching.ezgg.domain.memberInfo.entity.MemberInfo;
import com.matching.ezgg.domain.memberInfo.service.MemberInfoService;
import com.matching.ezgg.domain.recentTwentyMatch.entity.RecentTwentyMatch;
import com.matching.ezgg.domain.recentTwentyMatch.service.RecentTwentyMatchBuilderService;
import com.matching.ezgg.domain.recentTwentyMatch.service.RecentTwentyMatchService;
import com.matching.ezgg.domain.riotApi.dto.MatchDto;
import com.matching.ezgg.domain.recentTwentyMatch.dto.RecentTwentyMatchDto;
import com.matching.ezgg.domain.riotApi.dto.WinRateNTierDto;
import com.matching.ezgg.domain.memberInfo.dto.MemberInfoDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchingDataBulkSaveService {
	private final MemberInfoService memberInfoService;
	private final MatchInfoService matchInfoService;
	private final RecentTwentyMatchService recentTwentyMatchService;
	private final RecentTwentyMatchBuilderService recentTwentyMatchBuilderService;

	@Transactional
	public MemberInfoDto saveAllAggregatedData(
		Long memberId,
		WinRateNTierDto winRateNTierDto,
		List<String> fetchedMatchIds,
		List<MatchDto> matchDtoList,
		boolean existsNewMatchIds
	) {
		// memberInfo 업데이트
		MemberInfo memberInfo = memberInfoService.updateMemberInfo(memberId, winRateNTierDto, fetchedMatchIds, existsNewMatchIds);

		// matchInfo 업데이트
		// 어떤 matchInfo 저장 중 에러가 나왔는지 판단하기 위해 saveAll 적용X
		for (MatchDto matchDto : matchDtoList) {
			matchInfoService.save(matchDto);
		}

		return MemberInfoDto.toDto(memberInfo);
	}

	// recentTwentyMatch 계산 후 저장
	public RecentTwentyMatchDto calculateAndSaveRecentTwentyMatch(boolean existsNewMatchIds, String puuid, Long memberId){

		// recentTwentyMatch 계산
		RecentTwentyMatchDto recentTwentyMatchDto = new RecentTwentyMatchDto();
		if (existsNewMatchIds) recentTwentyMatchDto = recentTwentyMatchBuilderService.buildDto(puuid);

		// recentTwentyMatch 업데이트
		RecentTwentyMatch recentTwentyMatch;
		if (existsNewMatchIds) {
			recentTwentyMatch = recentTwentyMatchService.upsertRecentTwentyMatch(recentTwentyMatchDto);
		} else {
			recentTwentyMatch = recentTwentyMatchService.getRecentTwentyMatchByMemberId(memberId);
		}
		return RecentTwentyMatchDto.toDto(recentTwentyMatch);
	}




}
