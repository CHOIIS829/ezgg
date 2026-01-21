package com.matching.ezgg.domain.riotApi.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)  // Builder를 사용하여 객체를 생성할 때, 기존 객체를 기반으로 새로운 객체를 생성할 수 있도록 설정
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchDto {
	private long memberId;
	private String riotMatchId;
	private int kills;
	private int deaths;
	private int assists;
	private String teamPosition;
	private String championName;
	private boolean win;
	private List<String> matchKeywords;
}
