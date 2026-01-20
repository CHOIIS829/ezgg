package com.matching.ezgg.domain.timeline.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.matching.ezgg.domain.matchInfo.dto.TimelineMatchInfoDto;
import com.matching.ezgg.domain.matchInfo.service.MatchInfoService;
import com.matching.ezgg.domain.memberInfo.dto.TimelineMemberInfoDto;
import com.matching.ezgg.domain.memberInfo.service.MemberInfoService;
import com.matching.ezgg.domain.review.dto.ReviewTimelineResponseDto;
import com.matching.ezgg.domain.riotApi.service.ApiService;
import com.matching.ezgg.domain.riotApi.util.MatchMapper;
import com.matching.ezgg.domain.timeline.dto.DuoTimelineDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class DuoTimelineService {

	private final ApiService apiService;
	private final MatchMapper matchMapper;
	private final MemberInfoService memberInfoService;
	private final MatchInfoService matchInfoService;

	public List<DuoTimelineDto> getDuoMatchTimeline(List<ReviewTimelineResponseDto> dto) {
		List<DuoTimelineDto> duoTimelineDtoList = new ArrayList<>();

		for (ReviewTimelineResponseDto reviewTimelineResponseDto : dto) {
			Long memberId = reviewTimelineResponseDto.getMemberId();
			Long duoMemberId = reviewTimelineResponseDto.getPartnerMemberId();
			String matchId = reviewTimelineResponseDto.getMatchId();

			// Riot Api - Timeline 정보 가져오기
			JsonNode matchTimelineInfo = apiService.getDuoMatchTimeline(matchId).path("info");

			// memberId, matchId => timeline 정보 가져오기
			TimelineMemberInfoDto timelineMemberInfoDto = memberInfoService.getTimelineMemberInfoByMemberId(memberId);
			TimelineMemberInfoDto duoTimelineMemberInfoDto = memberInfoService.getTimelineMemberInfoByMemberId(duoMemberId);
			TimelineMatchInfoDto timelineMatchInfoDto = matchInfoService.getTimelineMemberInfoByMemberIdAndRiotMatchId(
				memberId, matchId
			);
			TimelineMatchInfoDto duoTimelineMatchInfoDto = matchInfoService.getTimelineMemberInfoByMemberIdAndRiotMatchId(
				duoMemberId, matchId
			);

			duoTimelineDtoList.add(matchMapper.extractDuoTimelineDto(
				matchTimelineInfo, timelineMemberInfoDto, duoTimelineMemberInfoDto, timelineMatchInfoDto,
				duoTimelineMatchInfoDto
			));
		}


		return duoTimelineDtoList;
	}
}
