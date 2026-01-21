package com.matching.ezgg.global.exception;

public class TokenCreateFailException extends BaseException {
	private static final String MESSAGE = "토큰 생성에 실패했습니다.";

	public TokenCreateFailException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return 500; // Conflict
	}
}
