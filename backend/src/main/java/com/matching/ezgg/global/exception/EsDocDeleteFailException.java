package com.matching.ezgg.global.exception;

public class EsDocDeleteFailException extends BaseException {
	private static final String MESSAGE = "Elasticsearch Doc 삭제를 실패했습니다.";

	public EsDocDeleteFailException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return 400;
	}
}
