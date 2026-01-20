package com.matching.ezgg.global.exception;

public class RiotApiException extends BaseException {

	public RiotApiException(String message) {
		super(message);
	}

	@Override
	public int getStatusCode() {
		return 502;
	}
}
