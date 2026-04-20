package com.sparta.deliveryorderplatform.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

	private int status; 	// HTTP 상태 코드
	private String message; // "SUCCESS", "VALIDATION_ERROR" 등
	private T data;			// 실제 응답 데이터

	// 성공 응답(반환 데이터 있는 경우)
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(200, "SUCCESS", data);
	}

	// 성공 응답(반환 데이터 없는 경우)
	public static <T> ApiResponse<T> success() {
		return new ApiResponse<>(200, "SUCCESS", null);
	}

	// 에러 응답
	public static ApiResponse<?> fail(int status, String message) {
		return new ApiResponse<>(status, message, null);
	}
}
