package com.sparta.deliveryorderplatform.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.deliveryorderplatform.auth.dto.LoginRequestDto;
import com.sparta.deliveryorderplatform.auth.dto.LoginResponseDto;
import com.sparta.deliveryorderplatform.auth.dto.SignUpRequestDto;
import com.sparta.deliveryorderplatform.auth.service.AuthService;
import com.sparta.deliveryorderplatform.global.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/auth/signup")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignUpRequestDto requestDto) {
		authService.signup(requestDto);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ApiResponse.success());
	}

	@PostMapping("/login")
	private ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto requestDto) {
		LoginResponseDto response = authService.login(requestDto);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(
		@AuthenticationPrincipal UserDetails userDetails
	) {
		authService.logout(userDetails.getUsername());
		return ResponseEntity.ok(ApiResponse.success());
	}
}
