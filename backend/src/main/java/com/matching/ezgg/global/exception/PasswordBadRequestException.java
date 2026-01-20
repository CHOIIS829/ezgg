package com.matching.ezgg.global.exception;

public class PasswordBadRequestException extends BaseException {

	private static final String MESSAGE = "패스워드 4~20자의 영문 대소문자와 숫자만 사용 가능합니다.";

	public PasswordBadRequestException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return 400;
	}
}
