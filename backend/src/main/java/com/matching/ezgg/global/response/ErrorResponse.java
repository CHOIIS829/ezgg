package com.matching.ezgg.global.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ErrorResponse {
	String code;
	String message;
}
