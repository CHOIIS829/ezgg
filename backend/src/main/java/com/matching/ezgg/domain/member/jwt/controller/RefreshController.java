package com.matching.ezgg.domain.member.jwt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.matching.ezgg.global.response.SuccessResponse;
import com.matching.ezgg.domain.member.jwt.sevice.RefreshService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RefreshController {

	private final RefreshService refreshService;

	@PostMapping("/refresh")
	public ResponseEntity<SuccessResponse<Void>> refresh(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = refreshService.validateAndExtractRefreshToken(request);
		RefreshService.TokenPair newTokens = refreshService.generateAndDeleteAndSaveNewTokens(refreshToken);

		response.setHeader("Authorization", newTokens.accessToken());
		response.addCookie(createCookie("Refresh", newTokens.refreshToken()));

		return ResponseEntity.ok(SuccessResponse.<Void>builder()
			.code("200")
			.message("토큰 재발급 성공")
			.build());
	}

	private Cookie createCookie(String key, String value) {
		Cookie cookie = new Cookie(key, value);
		cookie.setHttpOnly(true);
		cookie.setMaxAge(24 * 60 * 60);

		return cookie;
	}
}
