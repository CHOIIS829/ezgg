package com.matching.ezgg.global.exception;

public class EsPostFailException extends BaseException {
	private static final String MESSAGE = "Elasticsearch Post를 실패했습니다.";

	public EsPostFailException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return 400;
	}
}
