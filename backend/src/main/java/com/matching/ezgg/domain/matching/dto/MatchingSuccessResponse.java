package com.matching.ezgg.domain.matching.dto;

import com.matching.ezgg.domain.memberInfo.dto.MemberInfoDto;
import com.matching.ezgg.domain.recentTwentyMatch.dto.RecentTwentyMatchDto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MatchingSuccessResponse {
	private String status;
	private MatchedMemberData data;

	@Getter
	@Builder
	public static class MatchedMemberData {
		private Long matchedMemberId;
		private MemberInfoDto memberInfoDto;
		private RecentTwentyMatchDto recentTwentyMatchDto;
		private String chattingRoomId;
	}
}
