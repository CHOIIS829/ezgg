package com.matching.ezgg.domain.memberInfo.dto;

import java.util.List;

import com.matching.ezgg.domain.memberInfo.entity.MemberInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberInfoDto {
	private Long memberId;
	private String riotUsername;
	private String riotTag;
	private String puuid;
	private List<String> matchIds;
	private String tier;
	private String tierNum;
	private Integer wins;
	private Integer losses;

	public static MemberInfoDto toDto(MemberInfo memberInfo) {
		return MemberInfoDto.builder()
			.memberId(memberInfo.getMemberId())
			.riotUsername(memberInfo.getRiotUsername())
			.riotTag(memberInfo.getRiotTag())
			.puuid(memberInfo.getPuuid())
			.matchIds(memberInfo.getMatchIds())
			.tier(memberInfo.getTier())
			.tierNum(memberInfo.getTierNum())
			.wins(memberInfo.getWins())
			.losses(memberInfo.getLosses())
			.build();
	}
}
