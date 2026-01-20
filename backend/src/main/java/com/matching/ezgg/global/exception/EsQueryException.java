package com.matching.ezgg.global.exception;

public class EsQueryException extends BaseException {
	private static final String MESSAGE = "Elasticsearch 쿼리 실행 중 오류가 발생하였습니다.";

	public EsQueryException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return 500;
	}
}
