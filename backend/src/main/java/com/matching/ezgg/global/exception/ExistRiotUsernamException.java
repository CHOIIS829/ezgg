package com.matching.ezgg.global.exception;

public class ExistRiotUsernamException extends BaseException {
	private static final String MESSAGE = "이미 존재하는 라이엇 아이디입니다.";

	public ExistRiotUsernamException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return 409; // Conflict
	}
}
