package com.matching.ezgg.global.exception;

public class RetrySetException extends BaseException{
	private static final String MESSAGE = "Retry ZSET 작업 중 내부 오류가 발생했습니다.";

	// 상세 메시지·memberId·cause 를 모두 포함해 예외 생성
	public RetrySetException(Long memberId, String detail, Throwable cause) {
		super(String.format("%s (memberId: %d) - %s", MESSAGE, memberId, detail), cause);
	}

	@Override
	public int getStatusCode() {
		return 500;   // 서버 내부 오류
	}
}
