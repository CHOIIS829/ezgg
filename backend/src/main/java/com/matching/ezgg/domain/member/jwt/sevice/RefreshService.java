package com.matching.ezgg.domain.member.jwt.sevice;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.matching.ezgg.domain.member.entity.Member;
import com.matching.ezgg.domain.member.jwt.filter.JWTUtil;
import com.matching.ezgg.domain.member.jwt.repository.RedisRefreshTokenRepository;
import com.matching.ezgg.domain.member.service.MemberService;
import com.matching.ezgg.global.exception.InvalidTokenException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshService {

	private final JWTUtil jwtUtil;
	private final RedisRefreshTokenRepository redisRefreshTokenRepository;
	private final MemberService memberService;

	private final static long ACCESS_TOKEN_EXPIRY = 60 * 60 * 1000L; // 1시간
	private final static long REFRESH_TOKEN_EXPIRY = 24 * 60 * 60 * 1000L; // 24시간

	/**
	 * Refresh 토큰을 검증하고 추출하는 메서드
	 * @param request
	 * @return 검증된 Refresh 토큰
	 */
	public String validateAndExtractRefreshToken(HttpServletRequest request) {
		String refreshToken = extractCookie(request, "Refresh");
		String UUID = jwtUtil.getUUID(refreshToken);
		log.info(">>>>> refreshToken: {}", refreshToken);
		log.info(">>>>> uuid: {}", UUID);

		if (refreshToken == null) {
			log.info(">>>>> Refresh 토큰이 없습니다.");
			throw new InvalidTokenException();
		}

		if (!"refresh".equals(jwtUtil.getCategory(refreshToken))) {
			log.error(">>>>> refresh-token이 아닙니다.");
			throw new InvalidTokenException();
		}

		if (!redisRefreshTokenRepository.existsByRefreshToken(refreshToken)) {
			log.error(">>>>> Redis에 Refresh 토큰이 존재하지 않습니다.");
			throw new InvalidTokenException();
		}

		String savedRefreshToken = redisRefreshTokenRepository.findRefreshTokenByUUID(UUID);
		log.info(">>>>> Redis에 저장된 Refresh 토큰: {}", savedRefreshToken);

		if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
			log.error(">>>>> 유효하지 않은 Refresh 토큰입니다.");
			throw new InvalidTokenException();
		}

		return refreshToken;
	}

	/**
	 * Refresh 토큰을 사용하여 새로운 Access Token과 Refresh Token을 생성하는 메서드
	 * @param refreshToken
	 * @return 새로운 Access Token과 Refresh Token을 포함하는 TokenPair 객체
	 */
	public TokenPair generateAndDeleteAndSaveNewTokens(String refreshToken) {
		String UUID = jwtUtil.getUUID(refreshToken);// 토큰에서 uuid 추출
		String memberUsername = redisRefreshTokenRepository.findMemberUsernameByUUID(UUID);
		Member member = memberService.findMemberByUsername(memberUsername);// 회원정보 조회

		String newAccessToken = jwtUtil.accessCreateJwt("access", member.getId(), memberUsername, member.getRole(), ACCESS_TOKEN_EXPIRY);
		Map<String, String> refreshMap = jwtUtil.refreshCreateJwt("refresh", UUID, REFRESH_TOKEN_EXPIRY);

		// Redis에서 기존 Refresh Token 삭제 후 새로운 Refresh Token 저장
		redisRefreshTokenRepository.deleteByUUID(UUID);
		log.info(">>>>> Redis에서 기존 Refresh Token 삭제 완료");
		redisRefreshTokenRepository.save(UUID, memberUsername, refreshMap.get("refreshToken"), REFRESH_TOKEN_EXPIRY);
		log.info(">>>>> Redis에 새로운 Refresh Token 저장 완료");

		return new TokenPair(newAccessToken, refreshMap.get("refreshToken"));
	}

	/**
	 * HTTP 요청에서 쿠키를 추출하는 메서드
	 * @param request
	 * @param name
	 * @return 쿠키 값
	 */
	private String extractCookie(HttpServletRequest request, String name) {
		if (request.getCookies() == null) return null;
		for (Cookie cookie : request.getCookies()) {
			if (cookie.getName().equals(name)) {
				return cookie.getValue();
			}
		}
		return null;
	}

	/**
	 * Access Token과 Refresh Token을 포함하는 레코드 클래스
	 */
	public record TokenPair(String accessToken, String refreshToken) {}
}
