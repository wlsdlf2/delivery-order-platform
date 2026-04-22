package com.sparta.deliveryorderplatform.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	// 400 BAD_REQUEST(예시)
	VALIDATION_ERROR(400, "VALIDATION_ERROR", "요청 값이 올바르지 않습니다."),
	INVALID_PASSWORD(400, "INVALID_PASSWORD", "비밀번호가 올바르지 않습니다."),

	// 401 Unauthorized
	INVALID_TOKEN(401, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
	EXPIRED_TOKEN(401, "EXPIRED_TOKEN", "만료된 토큰입니다."),
	UNSUPPORTED_TOKEN(401, "UNSUPPORTED_TOKEN", "지원되지 않는 토큰입니다."),
	WRONG_TOKEN(401, "WRONG_TOKEN", "잘못된 토큰 서명입니다."),
	TOKEN_NOT_FOUND(401, "TOKEN_NOT_FOUND", "토큰을 찾을 수 없습니다.");

	private final int status;
	private final String code;
	private final String message;
}
