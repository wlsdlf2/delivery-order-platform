package com.sparta.deliveryorderplatform.auth.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.deliveryorderplatform.auth.dto.LoginRequestDto;
import com.sparta.deliveryorderplatform.auth.dto.LoginResponseDto;
import com.sparta.deliveryorderplatform.auth.dto.SignUpRequestDto;
import com.sparta.deliveryorderplatform.auth.jwt.JwtAuthenticationEntryPoint;
import com.sparta.deliveryorderplatform.auth.jwt.JwtTokenProvider;
import com.sparta.deliveryorderplatform.auth.service.AuthService;
import com.sparta.deliveryorderplatform.auth.service.LoginRateLimiter;
import com.sparta.deliveryorderplatform.auth.service.TokenBlacklistService;
import com.sparta.deliveryorderplatform.global.config.SecurityConfig;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.entity.UserRole;
import com.sparta.deliveryorderplatform.user.security.UserDetailsImpl;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class})
class AuthControllerTest {

	@Autowired MockMvc mockMvc;
	@Autowired ObjectMapper objectMapper;

	@MockBean AuthService authService;
	@MockBean JwtTokenProvider jwtTokenProvider;
	@MockBean TokenBlacklistService tokenBlacklistService;
	@MockBean LoginRateLimiter loginRateLimiter;
	@MockBean org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

	@BeforeEach
	void setUp() {
		given(loginRateLimiter.isAllowed(any())).willReturn(true);
	}

	// ─── 회원가입 ────────────────────────────────────────────────────────────

	@Test
	@DisplayName("회원가입 성공 - HTTP 201과 SUCCESS 메시지를 반환한다")
	void signup_success_returns201() throws Exception {
		SignUpRequestDto request = new SignUpRequestDto(
			"user1234", "Password1!", "닉네임", "test@example.com", UserRole.CUSTOMER
		);

		mockMvc.perform(post("/api/v1/auth/signup/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.message").value("SUCCESS"));
	}

	@Test
	@DisplayName("회원가입 실패 - 아이디 형식 위반 시 400과 검증 에러를 반환한다")
	void signup_invalidUsername_returns400() throws Exception {
		// 아이디 최소 4자 미만 (2자)
		SignUpRequestDto request = new SignUpRequestDto(
			"ab", "Password1!", "닉네임", "test@example.com", UserRole.CUSTOMER
		);

		mockMvc.perform(post("/api/v1/auth/signup/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
			.andExpect(jsonPath("$.errors").isArray());
	}

	@Test
	@DisplayName("회원가입 실패 - 비밀번호 형식 위반 시 400과 검증 에러를 반환한다")
	void signup_invalidPassword_returns400() throws Exception {
		// 대문자 미포함
		SignUpRequestDto request = new SignUpRequestDto(
			"user1234", "password1!", "닉네임", "test@example.com", UserRole.CUSTOMER
		);

		mockMvc.perform(post("/api/v1/auth/signup/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
			.andExpect(jsonPath("$.errors").isArray());
	}

	@Test
	@DisplayName("회원가입 실패 - 중복 아이디 시 400과 DUPLICATE_USERNAME 코드를 반환한다")
	void signup_duplicateUsername_returns400() throws Exception {
		SignUpRequestDto request = new SignUpRequestDto(
			"user1234", "Password1!", "닉네임", "test@example.com", UserRole.CUSTOMER
		);
		willThrow(new CustomException(ErrorCode.DUPLICATE_USERNAME))
			.given(authService).signup(any());

		mockMvc.perform(post("/api/v1/auth/signup/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("DUPLICATE_USERNAME"));
	}

	@Test
	@DisplayName("회원가입 실패 - 중복 이메일 시 400과 DUPLICATE_EMAIL 코드를 반환한다")
	void signup_duplicateEmail_returns400() throws Exception {
		SignUpRequestDto request = new SignUpRequestDto(
			"user1234", "Password1!", "닉네임", "test@example.com", UserRole.CUSTOMER
		);
		willThrow(new CustomException(ErrorCode.DUPLICATE_EMAIL))
			.given(authService).signup(any());

		mockMvc.perform(post("/api/v1/auth/signup/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"));
	}

	@Test
	@DisplayName("회원가입 실패 - MASTER 권한 선택 시 400과 INVALID_ROLE_SELECTION 코드를 반환한다")
	void signup_masterRole_returns400() throws Exception {
		SignUpRequestDto request = new SignUpRequestDto(
			"user1234", "Password1!", "닉네임", "test@example.com", UserRole.MASTER
		);
		willThrow(new CustomException(ErrorCode.INVALID_ROLE_SELECTION))
			.given(authService).signup(any());

		mockMvc.perform(post("/api/v1/auth/signup/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_ROLE_SELECTION"));
	}

	// ─── 로그인 ──────────────────────────────────────────────────────────────

	@Test
	@DisplayName("로그인 성공 - 200과 액세스·리프레시 토큰을 반환한다")
	void login_success_returns200WithTokens() throws Exception {
		LoginRequestDto request = new LoginRequestDto("user1234", "Password1!");
		UserRole role = UserRole.CUSTOMER;
		given(authService.login(any())).willReturn(LoginResponseDto.of("accessToken", "refreshToken", request.username(), role));

		mockMvc.perform(post("/api/v1/auth/signup/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("SUCCESS"))
			.andExpect(jsonPath("$.data.accessToken").value("accessToken"))
			.andExpect(jsonPath("$.data.refreshToken").value("refreshToken"))
			.andExpect(jsonPath("$.data.username").value("user1234"))
			.andExpect(jsonPath("$.data.role").value(role.name()));
	}

	@Test
	@DisplayName("로그인 실패 - 잘못된 자격증명 시 400과 LOGIN_FAILED 코드를 반환한다")
	void login_wrongCredentials_returns400() throws Exception {
		LoginRequestDto request = new LoginRequestDto("user1234", "WrongPass1!");
		willThrow(new CustomException(ErrorCode.LOGIN_FAILED))
			.given(authService).login(any());

		mockMvc.perform(post("/api/v1/auth/signup/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("LOGIN_FAILED"));
	}

	@Test
	@DisplayName("로그인 실패 - 아이디 미입력 시 400과 검증 에러를 반환한다")
	void login_blankUsername_returns400() throws Exception {
		LoginRequestDto request = new LoginRequestDto("", "Password1!");

		mockMvc.perform(post("/api/v1/auth/signup/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	// ─── 로그아웃 ─────────────────────────────────────────────────────────────

	@Test
	@DisplayName("로그아웃 성공 - 인증된 사용자가 200 응답을 받는다")
	void logout_authenticated_returns200() throws Exception {
		User user = User.createUser("user1234", "닉네임", "test@example.com", "encodedPw", UserRole.CUSTOMER);
		UserDetailsImpl userDetails = new UserDetailsImpl(user);
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
			userDetails, null, userDetails.getAuthorities()
		);

		mockMvc.perform(post("/api/v1/auth/signup/logout")
				.with(authentication(auth)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("SUCCESS"));
	}

	@Test
	@DisplayName("로그아웃 실패 - 비인증 상태에서 401과 TOKEN_NOT_FOUND 코드를 반환한다")
	void logout_unauthenticated_returns401() throws Exception {
		mockMvc.perform(post("/api/v1/auth/signup/logout"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("TOKEN_NOT_FOUND"));
	}
}
