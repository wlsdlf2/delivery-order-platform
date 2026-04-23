package com.sparta.deliveryorderplatform.auth.dto;

public record LoginResponseDto(
	String accessToken,
	String refreshToken
) {
	public static LoginResponseDto of(String accessToken, String refreshToken) {
		return new LoginResponseDto(accessToken, refreshToken);
	}
}