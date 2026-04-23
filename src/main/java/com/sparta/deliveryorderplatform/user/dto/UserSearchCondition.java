package com.sparta.deliveryorderplatform.user.dto;

import com.sparta.deliveryorderplatform.user.entity.UserRole;

public record UserSearchCondition(
	String username,
	String nickname,
	UserRole role
) {}
