package com.sparta.deliveryorderplatform.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	// 에러별 enum값 추가하여 사용
	// 400 BAD_REQUEST(예시)
	VALIDATION_ERROR(400, "VALIDATION_ERROR", "요청 값이 올바르지 않습니다."),
	INVALID_PASSWORD(400, "INVALID_PASSWORD", "비밀번호가 올바르지 않습니다."),

	// menu
	MENU_NOT_FOUND(404, "MENU_NOT_FOUND", "해당 상품이 존재하지 않습니다."),
  
	// payment
	INVALID_PAYMENT_METHOD(400, "INVALID_PAYMENT_METHOD", "올바른 결제 수단을 선택해 주세요"),
	PAYMENT_NOT_FOUND(404, "PAYMENT_NOT_FOUND", "결제 정보를 찾을 수 없습니다."),
	ALREADY_CANCELLED(400, "ALREADY_CANCELLED", "이미 취소된 결제입니다."),
	INVALID_PAYMENT_STATUS(400, "INVALID_PAYMENT_STATUS", "유효하지 않은 결제 상태 변경입니다.");

	private final int status;
	private final String code;
	private final String message;
}
