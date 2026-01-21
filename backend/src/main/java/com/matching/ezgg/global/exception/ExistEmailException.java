package com.matching.ezgg.global.exception;

public class ExistEmailException extends BaseException {

	private static final String MESSAGE = "이미 존재하는 회원 이메일입니다.";

	public ExistEmailException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return 409; // Conflict
	}
}
