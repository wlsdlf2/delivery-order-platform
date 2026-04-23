package com.sparta.deliveryorderplatform.user.entity;

import lombok.Getter;

@Getter
public enum UserRole {
	CUSTOMER("ROLE_CUSTOMER"), 	// 고객 권한
	OWNER("ROLE_OWNER"),		// 가게 사장 권한
	MASTER("ROLE_MASTER");		// 관리자 권한

	private final String authority;

	UserRole(String authority) {
		this.authority = authority;
	}

	public String getAuthority() {
		return this.authority;
	}

	public static class Authority {
		public static final String CUSTOMER = "ROLE_CUSTOMER";
		public static final String OWNER = "ROLE_OWNER";
		public static final String MANAGER = "ROLE_MANAGER";
		public static final String MASTER = "ROLE_MASTER";
	}
}
