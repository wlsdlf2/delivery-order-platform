package com.sparta.deliveryorderplatform.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequestDto(
	@NotBlank(message = "아이디를 입력해주세요.")
	@Pattern(regexp = "^[a-z0-9]{4,10}$", message = "아이디 형식이 올바르지 않습니다.")
	String username,

	@NotBlank(message = "비밀번호를 입력해주세요.")
	String password
) {}
