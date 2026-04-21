package com.sparta.deliveryorderplatform.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	// 에러별 enum값 추가하여 사용
	// 400 BAD_REQUEST(예시)
	VALIDATION_ERROR(400, "VALIDATION_ERROR", "요청 값이 올바르지 않습니다."),
	INVALID_PASSWORD(400, "INVALID_PASSWORD", "비밀번호가 올바르지 않습니다.");

	private final int status;
	private final String code;
	private final String message;
}
