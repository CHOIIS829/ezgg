package com.matching.ezgg.global.exception;

public class EsDocNotFoundException extends BaseException {
	private static final String FORMAT = "Elasticsearch Doc 조회를 실패했습니다. (memberId: %s)";

	public EsDocNotFoundException(Long memberId) {
		super(String.format(FORMAT, memberId));
	}

	@Override
	public int getStatusCode() {
		return 404;
	}
}
