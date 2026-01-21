package com.matching.ezgg.global.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.matching.ezgg.global.exception.BaseException;
import com.matching.ezgg.global.response.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ExceptionController {

	@ExceptionHandler(BaseException.class)
	public ResponseEntity<ErrorResponse> exceptionHandler(BaseException e) {
		log.error(">>>>> [ERROR] {}, {}", e.getStatusCode(), e.getMessage());
		int statusCode = e.getStatusCode();

		ErrorResponse body = ErrorResponse.builder()
			.code(String.valueOf(e.getStatusCode()))
			.message(e.getMessage())
			.build();

		return ResponseEntity.status(statusCode).body(body);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
		log.error(">>>>> [ERROR] {}, {}", 400, e.getMessage());
		ErrorResponse body = ErrorResponse.builder()
			.code("400")
			.message(e.getFieldError().getDefaultMessage())
			.build();

		return ResponseEntity.badRequest().body(body);
	}
}
