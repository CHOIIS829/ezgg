package com.matching.ezgg.global.exception;

public class MatchNotFoundException extends BaseException {
	private static final String MESSAGE = "Match 정보를 찾을 수 없습니다.";

	public MatchNotFoundException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return 404;
	}
}
