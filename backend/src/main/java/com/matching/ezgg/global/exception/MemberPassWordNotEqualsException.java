package com.matching.ezgg.global.exception;

public class MemberPassWordNotEqualsException extends BaseException {

	private static final String MESSAGE = "패스워드와 패스워드 확인이 일치하지 않습니다.";

	public MemberPassWordNotEqualsException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return 409; // Conflict
	}
}
