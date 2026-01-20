package com.matching.ezgg.global.exception;

public class MemberNotFoundException extends BaseException {
	private static final String MESSAGE = "존재하지 않는 회원입니다.";

	public MemberNotFoundException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return 404;
	}

}

