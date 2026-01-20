package com.matching.ezgg.global.exception;

public class EsAccessFailException extends BaseException {
	private static final String MESSAGE = "Elasticsearch 접근 중 내부 오류가 발생했습니다.";

	public EsAccessFailException(Throwable cause) { super(MESSAGE, cause); }

	@Override
	public int getStatusCode() { return 500; }
}
