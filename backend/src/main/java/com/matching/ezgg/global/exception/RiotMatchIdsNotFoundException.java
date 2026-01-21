package com.matching.ezgg.global.exception;

public class RiotMatchIdsNotFoundException extends BaseException {
	private static final String FORMAT = "최근 랭크 경기 전적을 찾을 수 없습니다. (puuid: %s)";

	public RiotMatchIdsNotFoundException(String puuid) {
		super(String.format(FORMAT, puuid));
	}

	@Override
	public int getStatusCode() {
		return 404;
	}
}
