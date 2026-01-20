package com.matching.ezgg.global.exception;

public class NullChampionsException extends BaseException {
	private static final String MESSAGE = "선호/비선호 챔피언이 모두 null입니다.";

	public NullChampionsException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return 500;
	}
}

