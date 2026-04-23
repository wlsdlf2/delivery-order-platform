package com.sparta.deliveryorderplatform.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	VALIDATION_ERROR(400, "VALIDATION_ERROR", "요청 값이 올바르지 않습니다."),

	// auth
	LOGIN_FAILED(400, "LOGIN_FAILED", "아이디 또는 비밀번호가 일치하지 않습니다."),
	INVALID_ROLE_SELECTION(400, "INVALID_ROLE_SELECTION", "가입할 수 없는 권한입니다."),
  	UNAUTHORIZED_ACCESS(403, "UNAUTHORIZED_ACCESS", "접근 권한이 없습니다."),
	ACCESS_DENIED(403, "ACCESS_DENIED", "해당 요청에 대한 권한이 없습니다."),

	// --- 유저 관련 (USER) ---
	USER_NOT_FOUND(404, "USER_NOT_FOUND", "해당 사용자를 찾을 수 없습니다."),
	DUPLICATE_USERNAME(400, "DUPLICATE_USERNAME", "이미 존재하는 아이디입니다."),
	DUPLICATE_EMAIL(400, "DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다."),
	INVALID_PASSWORD(400, "INVALID_PASSWORD", "비밀번호가 일치하지 않습니다."),

	// menu
	MENU_NOT_FOUND(404, "MENU_NOT_FOUND", "해당 상품이 존재하지 않습니다."),

	// category
  	DUPLICATE_CATEGORY_NAME(400, "DUPLICATE_CATEGORY_NAME", "이미 존재하는 카테고리명입니다."),
  	CATEGORY_NOT_FOUND(404, "CATEGORY_NOT_FOUND", "존재하지 않거나 삭제된 카테고리입니다."),

	// area
	DUPLICATE_AREA_NAME(400, "DUPLICATE_AREA_NAME", "이미 존재하는 운영지역명입니다."),
	AREA_NOT_FOUND(404, "AREA_NOT_FOUND", "존재하지 않거나 삭제된 운영지역입니다."),

	// store
	STORE_NOT_FOUND(404, "STORE_NOT_FOUND", "존재하지 않거나 삭제된 가게입니다."),

	// payment
	INVALID_PAYMENT_METHOD(400, "INVALID_PAYMENT_METHOD", "올바른 결제 수단을 선택해 주세요"),
	PAYMENT_NOT_FOUND(404, "PAYMENT_NOT_FOUND", "결제 정보를 찾을 수 없습니다."),
	ALREADY_CANCELLED(400, "ALREADY_CANCELLED", "이미 취소된 결제입니다."),
	INVALID_PAYMENT_STATUS(400, "INVALID_PAYMENT_STATUS", "유효하지 않은 결제 상태 변경입니다."),

	// jwt
	INVALID_TOKEN(401, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
	EXPIRED_TOKEN(401, "EXPIRED_TOKEN", "만료된 토큰입니다."),
	UNSUPPORTED_TOKEN(401, "UNSUPPORTED_TOKEN", "지원되지 않는 토큰입니다."),
	WRONG_TOKEN(401, "WRONG_TOKEN", "잘못된 토큰 서명입니다."),
	TOKEN_NOT_FOUND(401, "TOKEN_NOT_FOUND", "토큰을 찾을 수 없습니다.");

	private final int status;
	private final String code;
	private final String message;
}
