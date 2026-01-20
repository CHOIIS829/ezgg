package com.matching.ezgg.global.exception;

public class RiotMatchNotFoundException extends BaseException {
	private static final String FORMAT = "경기 정보를 찾을 수 없습니다. (matchId: %s)";

	public RiotMatchNotFoundException(String matchId) {
		super(String.format(FORMAT, matchId));
	}

	@Override
	public int getStatusCode() {
		return 404;
	}
}
