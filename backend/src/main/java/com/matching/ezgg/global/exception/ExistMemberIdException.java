package com.matching.ezgg.global.exception;

public class ExistMemberIdException extends BaseException {

	private static final String MESSAGE = "이미 존재하는 회원 ID입니다.";

	public ExistMemberIdException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return 409;
	}
}
