package com.matching.ezgg.global.exception;

import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {

	public BaseException(String message) {
		super(message);
	}

	public BaseException(String message, Throwable cause) {
		super(message, cause);   // ← 추가
	}

	public abstract int getStatusCode();
}
