package com.sparta.deliveryorderplatform.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.deliveryorderplatform.auth.dto.LoginRequestDto;
import com.sparta.deliveryorderplatform.auth.dto.LoginResponseDto;
import com.sparta.deliveryorderplatform.auth.dto.SignUpRequestDto;
import com.sparta.deliveryorderplatform.auth.service.AuthService;
import com.sparta.deliveryorderplatform.auth.service.LoginRateLimiter;
import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/auth/signup")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final LoginRateLimiter loginRateLimiter;

	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignUpRequestDto requestDto) {
		authService.signup(requestDto);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ApiResponse.success());
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponseDto>> login(
		@Valid @RequestBody LoginRequestDto requestDto,
		HttpServletRequest request
	) {
		if (!loginRateLimiter.isAllowed(getClientIp(request))) {
			throw new CustomException(ErrorCode.RATE_LIMIT_EXCEEDED);
		}
		LoginResponseDto response = authService.login(requestDto);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(
		@AuthenticationPrincipal UserDetails userDetails,
		HttpServletRequest request
	) {
		String bearer = request.getHeader("Authorization");
		String token = (bearer != null && bearer.startsWith("Bearer ")) ? bearer.substring(7) : null;
		authService.logout(userDetails.getUsername(), token);
		return ResponseEntity.ok(ApiResponse.success());
	}

	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<LoginResponseDto>> refresh(
		@RequestHeader("X-Refresh-Token") String refreshToken
	) {
		LoginResponseDto response = authService.refresh(refreshToken);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	private String getClientIp(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}
