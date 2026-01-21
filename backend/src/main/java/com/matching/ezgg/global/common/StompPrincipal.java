package com.matching.ezgg.global.common;

import java.security.Principal;

public class StompPrincipal implements Principal {
	private final Long userId;

	public StompPrincipal(Long userId) {
		this.userId = userId;
	}

	@Override
	public String getName() {
		return userId.toString();
	}
}
