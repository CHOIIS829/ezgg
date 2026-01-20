package com.matching.ezgg.domain.cancelPenalty;

import org.springframework.stereotype.Service;

import com.matching.ezgg.domain.matching.infra.redis.service.RedisService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CancelPenaltyService {

	private final RedisService redisService;

	public int getCancelCount(Long memberId) {
		return redisService.getCancelCount(memberId);
	}

	public int increaseCancelCount(Long memberId) {
		return redisService.increaseCancelCount(memberId);
	}

	public void resetCancelCount(Long memberId) {
		redisService.resetCancelCount(memberId);
	}
}
