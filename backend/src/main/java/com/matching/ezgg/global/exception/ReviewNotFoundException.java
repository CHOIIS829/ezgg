package com.matching.ezgg.global.exception;

public class ReviewNotFoundException extends BaseException {
	private static final String MESSAGE = "존재하지 않는 리뷰입니다.";

	public ReviewNotFoundException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return 404;
	}

}

