package com.matching.ezgg.global.exception;

public class RiotTierNotFoundException extends BaseException {
		private static final String FORMAT = "개인/2인 랭크 티어와 승률을 찾을 수 없습니다. (puuid: %s)";


	public RiotTierNotFoundException(String puuid) {
		super(String.format(FORMAT, puuid));
	}

	@Override
	public int getStatusCode() {
		return 404;
	}
}
