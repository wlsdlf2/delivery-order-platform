package com.sparta.deliveryorderplatform.user.dto;

import java.time.LocalDateTime;

import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.entity.UserRole;

public record UserResponseDto(
	String username,
	String nickname,
	String email,
	UserRole role,
	boolean isPublic,
	LocalDateTime createdAt
) {
	public static UserResponseDto from(User user) {
		return new UserResponseDto(
			user.getUsername(),
			user.getNickname(),
			user.getEmail(),
			user.getRole(),
			user.getIsPublic(),
			user.getCreatedAt()
		);
	}
}
