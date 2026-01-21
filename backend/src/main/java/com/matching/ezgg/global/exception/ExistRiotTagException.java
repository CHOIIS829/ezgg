package com.matching.ezgg.global.exception;

public class ExistRiotTagException extends BaseException {
	private static final String MESSAGE = "이미 존재하는 라이엇 태그입니다.";

	public ExistRiotTagException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return 409; // Conflict
	}
}
