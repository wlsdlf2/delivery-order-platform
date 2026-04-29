package com.sparta.deliveryorderplatform.user.entity;

import com.sparta.deliveryorderplatform.global.entity.BaseAuditEntity;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE)
public class User extends BaseAuditEntity {
	@Id
	@Column(length = 10, nullable = false)
	private String username; // 사용자 ID

	@Column(nullable = false, length = 100)
	private String nickname; // 사용자 닉네임

	@Column(nullable = false, length = 255)
	private String email;	 // 사용자 이메일

	@Column(nullable = false, length = 255)
	private String password; // 사용자 비밀번호

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private UserRole role;	 // 사용자 권한

	@Builder.Default
	@Column(nullable = false)
	private Boolean isPublic = true;	// 사용자 정보 공개 여부(기본값 true)

	public static User createUser(String username, String nickname, String email, String password, UserRole role) {
		if (username == null || username.isBlank()) throw new CustomException(ErrorCode.VALIDATION_ERROR);
		if (nickname == null || nickname.isBlank()) throw new CustomException(ErrorCode.VALIDATION_ERROR);
		if (password == null || password.isBlank()) throw new CustomException(ErrorCode.VALIDATION_ERROR);
		if (email == null || email.isBlank()) throw new CustomException(ErrorCode.VALIDATION_ERROR);
		if (role == null) throw new CustomException(ErrorCode.VALIDATION_ERROR);

		return User.builder()
			.username(username)
			.nickname(nickname)
			.email(email)
			.password(password)
			.role(role)
			.isPublic(true)
			.build();
	}

	public void updateProfile(String nickname, String email, Boolean isPublic) {
		if (nickname != null && !nickname.isBlank()) {
			this.nickname = nickname;
		}
		if (email != null && !email.isBlank()) {
			this.email = email;
		}
		if (isPublic != null) {
			this.isPublic = isPublic;
		}
	}

	public void updatePassword(String encodedPassword) {
		this.password = encodedPassword;
	}

	public void updateRole(UserRole newRole) {
		if (newRole == null) {
			throw new CustomException(ErrorCode.INVALID_ROLE_SELECTION);
		}

		this.role = newRole;
	}

	public static User reconstruct(String username, String nickname, String email,
			UserRole role, Boolean isPublic) {
		return User.builder()
			.username(username)
			.nickname(nickname)
			.email(email)
			.role(role)
			.isPublic(isPublic)
			.build();
	}
}
