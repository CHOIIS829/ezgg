package com.matching.ezgg.global.exception;

public class TokenNotFoundException extends BaseException {
	private static final String MESSAGE = "존재하지 않는 토큰입니다.";

	public TokenNotFoundException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return 404;
	}

}

