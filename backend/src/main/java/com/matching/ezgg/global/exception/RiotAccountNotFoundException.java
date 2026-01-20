package com.matching.ezgg.global.exception;

public class RiotAccountNotFoundException extends BaseException {
	private static final String FORMAT = "Riot 계정 %s#%s 을(를) 찾을 수 없습니다.";

	public RiotAccountNotFoundException(String riotId, String tag) {
		super(String.format(FORMAT, riotId, tag));
	}

	@Override
	public int getStatusCode() {
		return 404;
	}
}
