package com.matching.ezgg.domain.timeline.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.matching.ezgg.domain.review.dto.ReviewTimelineResponseDto;
import com.matching.ezgg.domain.review.service.ReviewService;
import com.matching.ezgg.domain.timeline.dto.DuoTimelineDto;
import com.matching.ezgg.domain.timeline.service.DuoTimelineService;
import com.matching.ezgg.global.annotation.LoginUser;
import com.matching.ezgg.global.response.SuccessResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/duo")
@RestController
public class DuoTimelineController {

	private final DuoTimelineService duoTimelineService;
	private final ReviewService reviewService;

	@GetMapping("/timeline")
	public ResponseEntity<SuccessResponse<List<DuoTimelineDto>>> getDuoTimeline(@LoginUser Long memberId) {
		List<ReviewTimelineResponseDto> dtos = reviewService.getRecentReviews(memberId);

		return ResponseEntity.ok(SuccessResponse.<List<DuoTimelineDto>>builder()
			.code("200")
			.message("듀오 타임라인 정보 조회 성공")
			.data(duoTimelineService.getDuoMatchTimeline(dtos))
			.build()
		);
	}
}
