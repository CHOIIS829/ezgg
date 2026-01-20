package com.matching.ezgg.domain.cancelPenalty;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.matching.ezgg.global.annotation.LoginUser;
import com.matching.ezgg.global.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RequestMapping("/cancel")
@RequiredArgsConstructor
@RestController
public class CancelPenaltyController {

	private final CancelPenaltyService cancelPenaltyService;

	@GetMapping("/count")
	public ResponseEntity<SuccessResponse<Integer>> getCancelCount(@LoginUser Long memberId) {
		return ResponseEntity.ok(SuccessResponse.<Integer>builder()
			.code("200")
			.message("매칭 취소 횟수 조회 성공")
			.data(cancelPenaltyService.getCancelCount(memberId))
			.build()
		);
	}

	@GetMapping("/increase")
	public ResponseEntity<SuccessResponse<Integer>> increaseCancelCount(@LoginUser Long memberId) {
		return ResponseEntity.ok(SuccessResponse.<Integer>builder()
			.code("200")
			.message("매칭 취소 횟수 조회 성공")
			.data(cancelPenaltyService.increaseCancelCount(memberId))
			.build()
		);
	}
}
