package com.matching.ezgg.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewDto {
	private String partnerRiotUsername;
	private String matchId;
	private int reviewScore;
}
