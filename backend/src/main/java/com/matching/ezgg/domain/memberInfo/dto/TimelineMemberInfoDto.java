package com.matching.ezgg.domain.memberInfo.dto;

import com.matching.ezgg.domain.memberInfo.entity.MemberInfo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TimelineMemberInfoDto {
	private Long memberId;
	private String riotUsername;
	private String riotTag;
	private String puuid;

	public static TimelineMemberInfoDto toDto(MemberInfo memberInfo) {
		return TimelineMemberInfoDto.builder()
			.memberId(memberInfo.getMemberId())
			.riotUsername(memberInfo.getRiotUsername())
			.riotTag(memberInfo.getRiotTag())
			.puuid(memberInfo.getPuuid())
			.build();
	}
}
