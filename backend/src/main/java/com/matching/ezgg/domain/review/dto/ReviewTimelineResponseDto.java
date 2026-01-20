package com.matching.ezgg.domain.review.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewTimelineResponseDto {

	private Long memberId;
	private Long partnerMemberId;
	private String matchId;
}
