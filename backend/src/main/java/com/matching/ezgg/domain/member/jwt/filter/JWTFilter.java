package com.matching.ezgg.domain.member.jwt.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matching.ezgg.domain.member.jwt.repository.RedisRefreshTokenRepository;
import com.matching.ezgg.domain.member.entity.Member;
import com.matching.ezgg.domain.member.jwt.dto.CustomUserDetails;
import com.matching.ezgg.global.response.ErrorResponse;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

	private final JWTUtil jwtUtil;
	private final RedisRefreshTokenRepository redisRefreshTokenRepository;
	private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 변환을 위한 ObjectMapper

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path.startsWith("/auth/logout") ||
			path.equals("/login") ||
			path.equals("/refresh");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		String accessToken = jwtUtil.extractTokenFromRequest(request);

		if (accessToken == null) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			if (!jwtUtil.isExpired(accessToken)) {
				// 토큰이 블랙리스트에 있는지 확인
				if (redisRefreshTokenRepository.isBlacklisted(accessToken)) {
					handleJwtException(response, "already logout", HttpServletResponse.SC_UNAUTHORIZED);
					return;
				}
				setAuthenticationToContext(accessToken);
			}
			filterChain.doFilter(request, response);
		} catch (ExpiredJwtException e) {
			handleJwtException(response, "token is expired", HttpServletResponse.SC_UNAUTHORIZED);
		} catch (SignatureException e) {
			handleJwtException(response, "유효하지 않은 JWT 서명입니다.", HttpServletResponse.SC_UNAUTHORIZED);
		} catch (MalformedJwtException e) {
			handleJwtException(response, "잘못된 JWT 형식입니다.", HttpServletResponse.SC_UNAUTHORIZED);
		} catch (UnsupportedJwtException e) {
			handleJwtException(response, "지원되지 않는 JWT 토큰입니다.", HttpServletResponse.SC_UNAUTHORIZED);
		} catch (JwtException e) {
			handleJwtException(response, "JWT 처리 중 오류가 발생했습니다.", HttpServletResponse.SC_UNAUTHORIZED);
		}
	}

	// JWT를 검증하고 인증 정보를 SecurityContext에 설정하는 메서드
	private void setAuthenticationToContext(String accessToken) {
		String memberUsername = jwtUtil.getMemberUsername(accessToken);
		String role = jwtUtil.getRole(accessToken);

		Member member = Member.builder()
			.memberUsername(memberUsername)
			.role(role)
			.id(jwtUtil.getMemberId(accessToken))
			.build();

		log.info(">>>>> JWTFilter memberId: {}", member.getId());
		log.info(">>>>> JWTFilter memberUsername: {}", member.getMemberUsername());
		log.info(">>>>> JWTFilter role: {}", member.getRole());
		CustomUserDetails userDetails = new CustomUserDetails(member);
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
			userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private void handleJwtException(HttpServletResponse response, String message, int status) throws IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setStatus(status);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.code(String.valueOf(status))
			.message(message)
			.build();

		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}

}
