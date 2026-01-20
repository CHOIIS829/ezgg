package com.matching.ezgg.global.exception;

public class TestException extends BaseException {

	private static final String MESSAGE = "테스트 예외입니다.";

	public TestException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return 400; // Bad Request
	}
}
