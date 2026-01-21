package com.matching.ezgg.domain.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.matching.ezgg.domain.matching.dto.MemberDataBundleDto;
import com.matching.ezgg.domain.matching.service.MemberDataBundleService;
import com.matching.ezgg.domain.member.dto.SignupRequest;
import com.matching.ezgg.domain.member.dto.SignupResponse;
import com.matching.ezgg.domain.member.service.MemberService;
import com.matching.ezgg.domain.memberInfo.dto.MemberInfoDto;
import com.matching.ezgg.domain.memberInfo.entity.MemberInfo;
import com.matching.ezgg.domain.memberInfo.service.MemberInfoService;
import com.matching.ezgg.global.annotation.LoginUser;
import com.matching.ezgg.global.response.SuccessResponse;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;
	private final MemberDataBundleService memberDataBundleService;
	private final MemberInfoService memberInfoService;

	//회원 가입
	@PostMapping("/signup")
	public ResponseEntity<SuccessResponse<SignupResponse>> signup(
		@Valid @RequestBody SignupRequest signupRequest
	) {
		SignupResponse signupResponse = memberService.signup(signupRequest);

		return ResponseEntity.ok(SuccessResponse.<SignupResponse>builder()
			.code("200")
			.message("회원가입 성공")
			.data(signupResponse)
			.build());
	}

	// 로그아웃
	@PostMapping("/logout")
	public ResponseEntity<SuccessResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
		log.info("[INFO]>>>>> 로그아웃 요청 들어옴");

		memberService.addBlackList(request);
		memberService.deleteRefreshToken(request);

		// 클라이언트 쿠키 삭제
		Cookie refreshCookie = new Cookie("Refresh", null);
		refreshCookie.setMaxAge(0); // 쿠키 즉시 만료
		refreshCookie.setHttpOnly(true);
		refreshCookie.setPath("/"); // 모든 경로에서 쿠키 삭제
		response.addCookie(refreshCookie);

		log.info("[INFO]>>>>> 로그아웃 성공");

		return ResponseEntity.ok(SuccessResponse.<Void>builder()
			.code("200")
			.message("로그아웃 성공")
			.build());
	}

	@PostMapping("/validateToken")
	public ResponseEntity<SuccessResponse<Void>> validateToken(HttpServletRequest request) {
		log.info("[INFO]>>>>> 토큰 검증 요청 들어옴");
		memberService.validateToken(request);

		return ResponseEntity.ok(SuccessResponse.<Void>builder()
			.code("200")
			.message("토큰 검증 성공")
			.build());
	}

	@GetMapping("/memberinfo")
	public ResponseEntity<SuccessResponse<MemberInfoDto>> getMemberInfo(@LoginUser Long memberId) {
		// 로그인한 사용자의 ID를 사용하여 회원 정보를 조회
		log.info("[INFO]>>>>> 로그인한 사용자의 ID: {}", memberId);
		MemberInfoDto loggedMemberInfo = memberInfoService.getMemberInfoByMemberId(memberId);

		return ResponseEntity.ok(SuccessResponse.<MemberInfoDto>builder()
			.code("200")
			.message("회원정보 조회 성공")
			.data(loggedMemberInfo)
			.build());
	}

	@GetMapping("/memberdatabundle")
	public ResponseEntity<SuccessResponse<MemberDataBundleDto>> getMemberDataBundle(@LoginUser Long memberId) {
		// 로그인한 사용자의 ID를 사용하여 회원 정보를 조회
		MemberDataBundleDto loggedMemberDataBundleDto = memberDataBundleService.getMemberDataBundleByMemberId(memberId);

		return ResponseEntity.ok(SuccessResponse.<MemberDataBundleDto>builder()
			.code("200")
			.message("회원정보 조회 성공")
			.data(loggedMemberDataBundleDto)
			.build());
	}
}
