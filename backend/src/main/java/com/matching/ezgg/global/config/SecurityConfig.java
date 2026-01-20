package com.matching.ezgg.global.config;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.matching.ezgg.domain.member.jwt.filter.JWTFilter;
import com.matching.ezgg.domain.member.jwt.filter.JWTUtil;
import com.matching.ezgg.domain.member.jwt.filter.LoginFilter;
import com.matching.ezgg.domain.member.jwt.repository.RedisRefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JWTUtil jwtUtil;
	private final AuthenticationConfiguration authenticationConfiguration;
	private final RedisRefreshTokenRepository redisRefreshTokenRepository;

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return web -> web.ignoring()
			.requestMatchers("/favicon.ico")
			.requestMatchers("/error")
			.requestMatchers(toH2Console());
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		// 기본 설정 비활성화
		http
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable);

		// URL 접근 권한 설정
		http.authorizeHttpRequests((auth) -> auth
			.requestMatchers("/auth/signup", "/auth/logout", "/login", "/refresh", "/ws/**", "/ws").permitAll() // 해당 요청 은 인증 없이 접근 가능
			.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
			.anyRequest().hasAnyAuthority("ROLE_USER")); // 나머지 요청은 ROLE_USER 권한이 있어야 접근 가능

		// 세션 관리 설정
		http.sessionManagement((session) ->
			session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		// JWT 필터 적용
		http.addFilterBefore(new JWTFilter(jwtUtil, redisRefreshTokenRepository), LoginFilter.class)
			.addFilterAt(
				new LoginFilter(jwtUtil, authenticationManager(authenticationConfiguration),
					redisRefreshTokenRepository),
				UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.addAllowedOrigin("http://localhost:3000");
		configuration.addAllowedMethod("*");
		configuration.addAllowedHeader("*");
		configuration.addExposedHeader("Authorization");
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
