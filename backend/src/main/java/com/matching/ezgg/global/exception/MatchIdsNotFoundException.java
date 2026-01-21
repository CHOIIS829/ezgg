package com.matching.ezgg.global.exception;

public class MatchIdsNotFoundException extends BaseException {
	private static final String MESSAGE = "매치 아이디를 찾을 수 없습니다.";

	public MatchIdsNotFoundException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return 404;
	}

}

