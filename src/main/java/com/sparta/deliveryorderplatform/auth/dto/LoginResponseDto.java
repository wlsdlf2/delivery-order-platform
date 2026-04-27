package com.sparta.deliveryorderplatform.auth.dto;

import com.sparta.deliveryorderplatform.user.entity.UserRole;

public record LoginResponseDto(
	String accessToken,
	String refreshToken,
	String username,
	UserRole role
) {
	public static LoginResponseDto of(String accessToken, String refreshToken, String username, UserRole role) {
		return new LoginResponseDto(accessToken, refreshToken, username, role);
	}
}