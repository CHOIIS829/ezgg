package com.matching.ezgg.domain.member.jwt.filter;

import static io.jsonwebtoken.Jwts.SIG.*;
import static java.nio.charset.StandardCharsets.*;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.matching.ezgg.global.exception.InvalidTokenException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JWTUtil {

	private final SecretKey key;

	public JWTUtil(@Value("${jwt.secret}") String key) {
		this.key = new SecretKeySpec(key.getBytes(UTF_8), HS256.key().build().getAlgorithm());
	}

	// 토큰 파싱 -> Claims 객체로 변환 (claims란 JWT의 payload에 해당하는 부분)
	private Claims parseClaims(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
	}

	// memberUsername 가져오기
	public String getMemberUsername(String token) {
		return parseClaims(token).get("memberUsername", String.class);
	}

	// memberId 가져오기
	public Long getMemberId(String token) {
		return parseClaims(token).get("memberId", Long.class);
	}

	// 권한 정보 가져오기
	public String getRole(String token) {
		return parseClaims(token).get("role", String.class);
	}

	// 카테고리 정보 가져오기 (access, refresh)
	public String getCategory(String token) {
		return parseClaims(token).get("category", String.class);
	}

	// RefreshToken의 UUID 가져오기
	public String getUUID(String token) {
		return parseClaims(token).get("uuid", String.class);
	}

	// 토큰 만료 여부 확인
	public Boolean isExpired(String token) {
		return parseClaims(token).getExpiration().before(new java.util.Date());
	}

	// 토큰의 만료 시간 가져오기 (밀리초 단위)
	public long getExpirationTime(String token) {
		return parseClaims(token).getExpiration().getTime();
	}

	// HTTP 요청 헤더에서 토큰 추출하기
	public String extractTokenFromRequest(HttpServletRequest request) {
		String token = request.getHeader("Authorization");
		log.debug("Authorization Header: {}", token);
		if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
			return token.substring(7);
		}
		return null;
	}

	public String accessCreateJwt(String category, Long memberId, String memberUsername, String role, Long expiredMs) {
		return Jwts.builder()
			.claim("category", category)
			.claim("memberUsername", memberUsername)
			.claim("role", role)
			.claim("memberId", memberId)
			.issuedAt(new java.util.Date(System.currentTimeMillis()))
			.expiration(new java.util.Date(System.currentTimeMillis() + expiredMs))
			.signWith(key)
			.compact();
	}

	public Map<String, String> refreshCreateJwt(String category, String UUID, Long expiredMs) {

		String token = Jwts.builder()
			.claim("category", category)
			.claim("uuid", UUID)
			.issuedAt(new Date(System.currentTimeMillis()))
			.expiration(new Date(System.currentTimeMillis() + expiredMs))
			.signWith(key)
			.compact();

		return Map.of("refreshToken", token, "uuid", UUID);
	}

}
