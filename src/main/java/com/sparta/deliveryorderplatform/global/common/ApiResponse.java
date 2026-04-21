package com.sparta.deliveryorderplatform.global.common;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.global.exception.FieldError;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

	private int status;                // HTTP 상태 코드
	private String code;
	private String message;            // "SUCCESS", "VALIDATION_ERROR" 등
	private T data;                        // 실제 응답 데이터
	private List<FieldError> errors;    // 에러 상세 내용

	// 성공 응답(반환 데이터 있는 경우)
	public static <T> ApiResponse<T> success (T data) {
		return new ApiResponse<>(200, null, "SUCCESS", data, null);
	}

	// 성공 응답(반환 데이터 없는 경우)
	public static <T> ApiResponse<T> success () {
		return new ApiResponse<>(200, null, "SUCCESS", null, null);
	}

	// 에러 응답
	public static ApiResponse<?> fail (ErrorCode errorCode) {
		return new ApiResponse<>(errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage(), null, null);
	}

	// 에러 응답
	public static ApiResponse<?> fail (ErrorCode errorCode, List<FieldError> errors) {
		return new ApiResponse<>(errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage(), null, errors);
	}
}
