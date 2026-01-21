package com.matching.ezgg.domain.matching.dto;

import com.matching.ezgg.domain.recentTwentyMatch.dto.RecentTwentyMatchDto;
import com.matching.ezgg.domain.memberInfo.dto.MemberInfoDto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberDataBundleDto {
	private MemberInfoDto memberInfoDto;
	private RecentTwentyMatchDto recentTwentyMatchDto;

	public static MemberDataBundleDto toDto(MemberInfoDto memberInfoDto, RecentTwentyMatchDto recentTwentyMatchDto) {
		return MemberDataBundleDto.builder()
			.memberInfoDto(memberInfoDto)
			.recentTwentyMatchDto(recentTwentyMatchDto)
			.build();
	}
}
