package com.matching.ezgg.global.exception;

public class InvalidTokenException extends BaseException {

	private static final String MESSAGE = "유효하지 않은 토큰입니다.";

	public InvalidTokenException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return 401;
	}
}
