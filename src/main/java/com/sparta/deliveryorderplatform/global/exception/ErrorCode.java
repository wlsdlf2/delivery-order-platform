package com.sparta.deliveryorderplatform.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	// 에러별 enum값 추가하여 사용
	// 400 BAD_REQUEST(예시)
	INVALID_PASSWORD(400, "비밀번호가 올바르지 않습니다."),
	INVALID_PAYMENT_METHOD(400, "올바른 결제 수단을 선택해 주세요"),
	PAYMENT_NOT_FOUND(404, "결제 정보를 찾을 수 없습니다.")
	;


	private final int status;
	private final String message;
}
