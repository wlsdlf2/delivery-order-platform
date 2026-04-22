package com.sparta.deliveryorderplatform.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
	CUSTOMER("ROLE_CUSTOMER"), 	// 고객 권한
	OWNER("ROLE_OWNER"),		// 가게 사장 권한
	MASTER("ROLE_MASTER");		// 관리자 권한

	private final String authority;
}
