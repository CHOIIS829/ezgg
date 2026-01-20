package com.matching.ezgg.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Value;

@JsonInclude(JsonInclude.Include.NON_NULL) // null인 필드
@Value
@Builder
public class SuccessResponse<T> {

	String code;
	String message;
	T data;
}
