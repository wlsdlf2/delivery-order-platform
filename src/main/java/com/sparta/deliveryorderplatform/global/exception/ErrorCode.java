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

    UNAUTHORIZED_ACCESS(403, "UNAUTHORIZED_ACCESS", "접근 권한이 없습니다."),

    DUPLICATE_CATEGORY_NAME(400, "DUPLICATE_CATEGORY_NAME", "이미 존재하는 카테고리명입니다."),
    CATEGORY_NOT_FOUND(404, "CATEGORY_NOT_FOUND", "존재하지 않거나 삭제된 카테고리입니다."),

	DUPLICATE_AREA_NAME(400, "DUPLICATE_AREA_NAME", "이미 존재하는 운영지역명입니다."),
	AREA_NOT_FOUND(404, "AREA_NOT_FOUND", "존재하지 않거나 삭제된 운영지역입니다."),

	STORE_NOT_FOUND(404, "STORE_NOT_FOUND", "존재하지 않거나 삭제된 가게입니다."),

	;


	private final int status;
	private final String code;
	private final String message;
}
