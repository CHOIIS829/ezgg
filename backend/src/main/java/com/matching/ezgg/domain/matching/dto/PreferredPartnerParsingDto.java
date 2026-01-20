package com.matching.ezgg.domain.matching.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PreferredPartnerParsingDto {

	@JsonProperty("wantLine")
	private WantLine wantLine;
	@JsonProperty("selectedChampions")
	private ChampionInfo championInfo;
	@Setter
	private String userPreferenceText;

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@ToString
	public static class WantLine {
		private String myLine;
		private String partnerLine;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@ToString
	public static class ChampionInfo {
		@JsonProperty("preferredChampions")
		private List<String> preferredChampions;
		@JsonProperty("bannedChampions")
		private List<String> unpreferredChampions;
	}
}
