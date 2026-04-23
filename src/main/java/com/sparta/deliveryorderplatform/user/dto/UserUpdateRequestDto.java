package com.sparta.deliveryorderplatform.user.dto;

import jakarta.validation.constraints.Email;

public record UserUpdateRequestDto(
		String nickname,

		@Email(message = "올바른 이메일 형식이 아닙니다.")
		String email,

		Boolean isPublic
) {}