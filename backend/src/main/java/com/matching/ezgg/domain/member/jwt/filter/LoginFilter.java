package com.matching.ezgg.domain.member.jwt.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matching.ezgg.domain.member.jwt.repository.RedisRefreshTokenRepository;
import com.matching.ezgg.domain.member.jwt.dto.CustomUserDetails;
import com.matching.ezgg.domain.member.jwt.dto.LoginRequest;
import com.matching.ezgg.global.response.ErrorResponse;
import com.matching.ezgg.global.response.SuccessResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

	private final JWTUtil jwtUtil;
	private final AuthenticationManager authenticationManager;
	private final RedisRefreshTokenRepository redisRefreshTokenRepository;

	// 로그인 요청을 처리하는 메서드
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws
		AuthenticationException {
		log.info(">>>>> 로그인 요청 들어옴");
		LoginRequest loginRequest = new LoginRequest();

		// x-www-form-urlencoded 방식의 로그인 요청을 json 데이터로 받기 위해 설정
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			ServletInputStream inputStream = request.getInputStream();
			String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
			loginRequest = objectMapper.readValue(messageBody, LoginRequest.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		String memberUsername = loginRequest.getMemberUsername();
		String password = loginRequest.getPassword();

		log.info(">>>>> 로그인 요청 memberId: {}", memberUsername);
		log.info(">>>>> 로그인 요청 password: {}", password);

		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(memberUsername, password);

		return authenticationManager.authenticate(token);
	}

	// 로그인 성공 시 호출되는 메서드
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
		Authentication authResult) throws IOException, ServletException {
		log.info(">>>>> 로그인 성공 {}", authResult.getName());

		CustomUserDetails userDetails = (CustomUserDetails)authResult.getPrincipal();
		Long memberId = userDetails.getMemberId();
		log.info(">>>>> 로그인 성공 {}", memberId);

		String memberUsername = authResult.getName();
		Collection<? extends GrantedAuthority> authorities = authResult.getAuthorities();
		Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
		GrantedAuthority auth = iterator.next();
		String role = auth.getAuthority();

		long accessTokenExpiry = 60 * 60 * 1000L; // 1시간 유효
		long refreshTokenExpiry = 24 * 60 * 60 * 1000L; // 1일 유효
		String UUID = java.util.UUID.randomUUID().toString();

		String accessToken = jwtUtil.accessCreateJwt("access", memberId, memberUsername, role, accessTokenExpiry);
		Map<String, String> refreshMap = jwtUtil.refreshCreateJwt("refresh", UUID, refreshTokenExpiry);

		// Redis에 Refresh Token 저장
		redisRefreshTokenRepository.save(refreshMap.get("uuid"), memberUsername, refreshMap.get("refreshToken"), refreshTokenExpiry);

		response.setHeader("Authorization", accessToken);
		response.addCookie(createCookie("Refresh", refreshMap.get("refreshToken")));

		response.setStatus(HttpStatus.OK.value());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Expose-Headers", "Authorization");

		SuccessResponse<Object> successResponse = SuccessResponse.builder()
			.code("200")
			.message("로그인 성공")
			.build();

		response.getWriter().write(new ObjectMapper().writeValueAsString(successResponse));
	}

	// 로그인 실패 시 호출되는 메서드
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException failed) throws IOException, ServletException {
		log.info(">>>>> 로그인 실패 {}", failed.getMessage());

		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
		response.setHeader("Access-Control-Allow-Credentials", "true");

		ErrorResponse errorResponse = ErrorResponse.builder()
			.code("401")
			.message("로그인 실패")
			.build();

		response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
	}

	private Cookie createCookie(String key, String value) {
		Cookie cookie = new Cookie(key, value);
		cookie.setHttpOnly(true); // JavaScript에서 접근 불가
		cookie.setMaxAge(24 * 60 * 60); // 1일

		return cookie;
	}
}
