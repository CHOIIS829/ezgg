package com.matching.ezgg.domain.riotApi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class WinRateNTierDto {
	private String queueType;

	private String puuid;
	private Integer wins;
	private Integer losses;
	private String tier;
	@JsonProperty("rank")
	private String tierNum;

	public static WinRateNTierDto unranked(String puuid) {
		return WinRateNTierDto.builder()
			.queueType("RANKED_SOLO_5x5")
			.puuid(puuid)
			.wins(0)
			.losses(0)
			.tier("UNRANKED")
			.tierNum("0")
			.build();
	}
}
