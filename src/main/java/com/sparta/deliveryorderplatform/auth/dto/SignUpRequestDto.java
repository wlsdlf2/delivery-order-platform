package com.sparta.deliveryorderplatform.auth.dto;

import com.sparta.deliveryorderplatform.user.entity.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SignUpRequestDto(
	@NotBlank(message = "아이디를 입력해주세요.")
	@Pattern(regexp = "^[a-z0-9]{4,10}$", message = "아이디는 4~1-자 영문 소문자, 숫자만 가능합니다.")
	String username,

	@NotBlank(message = "비밀번호를 입력해주세요.")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$",
			message = "비밀번호는 8~15자 영문 대소문자, 숫자, 특수문자를 포함해야 합니다.")
	String password,

	@NotBlank(message = "닉네임을 입력해주세요.")
	String nickname,

	@NotBlank(message = "이메일을 입력해주세요.")
	@Email
	String email,

	@NotNull(message = "권한을 선택해주세요.")
	UserRole role
) {}
