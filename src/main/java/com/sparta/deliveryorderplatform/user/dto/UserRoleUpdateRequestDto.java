package com.sparta.deliveryorderplatform.user.dto;

import com.sparta.deliveryorderplatform.user.entity.UserRole;

import jakarta.validation.constraints.NotNull;

public record UserRoleUpdateRequestDto(
	@NotNull(message = "변경할 권한을 선택해주세요.")
	UserRole role
) {}
