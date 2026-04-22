package com.sparta.deliveryorderplatform.user.entity;

import com.sparta.deliveryorderplatform.global.entity.BaseAuditEntity;
import com.sparta.deliveryorderplatform.user.enums.UserRole;

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
@AllArgsConstructor
@Builder
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
	private boolean isPublic = true;	// 사용자 정보 공개 여부(기본값 true)

	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}
}
