package com.matching.ezgg.domain.matching.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import com.matching.ezgg.domain.matching.dto.PreferredPartnerParsingDto;
import com.matching.ezgg.domain.matching.service.MatchingService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MatchingController {

	private final MatchingService matchingService;

	@MessageMapping("/matching/start")
	public void startMatching(@Payload
	PreferredPartnerParsingDto preferredPartnerDto, Principal principal) {
		if (principal == null)
			throw new IllegalArgumentException("로그인 정보가 없습니다.");
		Long memberId = Long.valueOf(principal.getName());
		matchingService.startMatching(memberId, preferredPartnerDto);
	}

	// 매칭 시도를 중단할 때 호출
	@MessageMapping("/matching/stop")
	public void stopMatching(Principal principal) {
		if (principal == null)
			throw new IllegalArgumentException("로그인 정보가 없습니다.");
		Long memberId = Long.valueOf(principal.getName());
		matchingService.stopMatching(memberId);
	}

	@MessageExceptionHandler
	@SendToUser("/queue/errors")
	public String handleException(Throwable throwable) {
		return throwable.getMessage();
	}
}
