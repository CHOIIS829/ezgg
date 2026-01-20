package com.matching.ezgg.domain.review.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.matching.ezgg.domain.review.dto.CreateReviewDto;
import com.matching.ezgg.domain.review.service.ReviewService;
import com.matching.ezgg.global.annotation.LoginUser;
import com.matching.ezgg.global.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;

	@PostMapping("/review")
	public ResponseEntity<SuccessResponse<Void>> createReview(@LoginUser Long memberId, @RequestBody CreateReviewDto createReviewDto) {
		reviewService.createReview(memberId, createReviewDto);

		return ResponseEntity.ok(SuccessResponse.<Void>builder()
			.code("200")
			.message("리뷰 작성 성공")
			.build());
	}

}
