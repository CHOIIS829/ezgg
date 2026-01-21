package com.matching.ezgg.global.exception;

public class RecentTwentyMatchNotFoundException extends BaseException {
	private static final String MESSAGE = "최근 20경기 요약 정보를 찾을 수 없습니다.";

	public RecentTwentyMatchNotFoundException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return 404;
	}
}
