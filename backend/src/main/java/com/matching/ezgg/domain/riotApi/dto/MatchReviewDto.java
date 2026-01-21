package com.matching.ezgg.domain.riotApi.dto;

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
public class MatchReviewDto {
	private String riotUsername;
	private String riotTag;
	private int teamId; // 100이면 블루 팀, 200이면 레드 팀
}
