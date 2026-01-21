package com.matching.ezgg.global.config;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import com.matching.ezgg.global.common.StompPrincipal;
import com.matching.ezgg.domain.member.jwt.filter.JWTUtil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomHandshakeHandler extends DefaultHandshakeHandler {

	private final JWTUtil jWTUtil;

	@Override
	protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
		Map<String, Object> attributes) {
		String token = (String) attributes.get("token");
		if (token != null && !jWTUtil.isExpired(token)) {
			Long memberId = jWTUtil.getMemberId(token);
			return new StompPrincipal(memberId);
		}
		return super.determineUser(request, wsHandler, attributes);
	}
}
