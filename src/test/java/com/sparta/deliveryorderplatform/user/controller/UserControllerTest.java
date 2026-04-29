package com.sparta.deliveryorderplatform.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.deliveryorderplatform.auth.jwt.JwtAuthenticationEntryPoint;
import com.sparta.deliveryorderplatform.auth.jwt.JwtTokenProvider;
import com.sparta.deliveryorderplatform.global.config.SecurityConfig;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.user.dto.PasswordChangeRequestDto;
import com.sparta.deliveryorderplatform.user.dto.UserResponseDto;
import com.sparta.deliveryorderplatform.user.dto.UserRoleUpdateRequestDto;
import com.sparta.deliveryorderplatform.user.dto.UserUpdateRequestDto;
import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.entity.UserRole;
import com.sparta.deliveryorderplatform.user.security.UserDetailsImpl;
import com.sparta.deliveryorderplatform.user.service.UserService;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class})
class UserControllerTest {

	@Autowired MockMvc mockMvc;
	@Autowired ObjectMapper objectMapper;

	@MockBean UserService userService;
	@MockBean JwtTokenProvider jwtTokenProvider;
	@MockBean org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
	@MockBean com.sparta.deliveryorderplatform.auth.service.TokenBlacklistService tokenBlacklistService;

	private Authentication authOf(User user) {
		UserDetailsImpl userDetails = new UserDetailsImpl(user);
		return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	}

	private Authentication masterAuth() {
		return authOf(User.createUser("master1", "관리자", "master@example.com", "encoded", UserRole.MASTER));
	}

	private Authentication customerAuth() {
		return authOf(User.createUser("user1234", "닉네임", "test@example.com", "encoded", UserRole.CUSTOMER));
	}

	private UserResponseDto sampleResponse() {
		return new UserResponseDto("user1234", "닉네임", "test@example.com", UserRole.CUSTOMER, true, LocalDateTime.now());
	}

	// ─── GET /api/v1/users ───────────────────────────────────────────────────

	@Test
	@DisplayName("사용자 목록 조회 성공 - MASTER 권한으로 200을 반환한다")
	void getUsers_master_returns200() throws Exception {
		given(userService.getUsers(any(), any())).willReturn(Page.empty());

		mockMvc.perform(get("/api/v1/users")
				.with(authentication(masterAuth())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("SUCCESS"));
	}

	@Test
	@DisplayName("사용자 목록 조회 실패 - 미인증 요청 시 401을 반환한다")
	void getUsers_unauthenticated_returns401() throws Exception {
		mockMvc.perform(get("/api/v1/users"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("사용자 목록 조회 실패 - CUSTOMER 권한으로 요청 시 403을 반환한다")
	void getUsers_customer_returns403() throws Exception {
		mockMvc.perform(get("/api/v1/users")
				.with(authentication(customerAuth())))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("사용자 목록 조회 성공 - keyword 파라미터로 조회하면 200을 반환한다")
	void getUsers_withKeyword_returns200() throws Exception {
		given(userService.getUsers(any(), any())).willReturn(Page.empty());

		mockMvc.perform(get("/api/v1/users")
				.param("keyword", "홍길동")
				.with(authentication(masterAuth())))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("사용자 목록 조회 성공 - 허용되지 않는 size는 10으로 보정되어 200을 반환한다")
	void getUsers_invalidSize_correctedTo10() throws Exception {
		given(userService.getUsers(any(), any())).willReturn(Page.empty());

		mockMvc.perform(get("/api/v1/users")
				.param("size", "7")
				.with(authentication(masterAuth())))
			.andExpect(status().isOk());
	}

	// ─── GET /api/v1/users/{username} ────────────────────────────────────────

	@Test
	@DisplayName("사용자 상세 조회 성공 - MASTER 권한으로 200을 반환한다")
	void getUser_master_returns200() throws Exception {
		given(userService.getUser(any(), any())).willReturn(sampleResponse());

		mockMvc.perform(get("/api/v1/users/user1234")
				.with(authentication(masterAuth())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.username").value("user1234"));
	}

	@Test
	@DisplayName("사용자 상세 조회 실패 - 미인증 요청 시 401을 반환한다")
	void getUser_unauthenticated_returns401() throws Exception {
		mockMvc.perform(get("/api/v1/users/user1234"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("사용자 상세 조회 성공 - CUSTOMER가 본인을 조회하면 200을 반환한다")
	void getUser_customer_self_returns200() throws Exception {
		given(userService.getUser(any(), any())).willReturn(sampleResponse());

		mockMvc.perform(get("/api/v1/users/user1234")
				.with(authentication(customerAuth())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.username").value("user1234"));
	}

	@Test
	@DisplayName("사용자 상세 조회 실패 - CUSTOMER가 타인을 조회하면 403을 반환한다")
	void getUser_customer_other_returns403() throws Exception {
		given(userService.getUser(any(), any()))
			.willThrow(new CustomException(ErrorCode.ACCESS_DENIED));

		mockMvc.perform(get("/api/v1/users/other999")
				.with(authentication(customerAuth())))
			.andExpect(status().isForbidden());
	}

	// ─── PUT /api/v1/users/{username} ────────────────────────────────────────

	@Test
	@DisplayName("사용자 정보 수정 성공 - 인증된 사용자가 200을 반환한다")
	void updateUser_authenticated_returns200() throws Exception {
		given(userService.updateUser(any(), any(), any())).willReturn(sampleResponse());
		UserUpdateRequestDto request = new UserUpdateRequestDto("새닉네임", null, null);

		mockMvc.perform(put("/api/v1/users/user1234")
				.with(authentication(customerAuth()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("SUCCESS"));
	}

	@Test
	@DisplayName("사용자 정보 수정 실패 - 미인증 요청 시 401을 반환한다")
	void updateUser_unauthenticated_returns401() throws Exception {
		UserUpdateRequestDto request = new UserUpdateRequestDto("닉네임", null, null);

		mockMvc.perform(put("/api/v1/users/user1234")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("사용자 정보 수정 실패 - 이메일 형식 위반 시 400과 VALIDATION_ERROR 코드를 반환한다")
	void updateUser_invalidEmail_returns400() throws Exception {
		UserUpdateRequestDto request = new UserUpdateRequestDto(null, "invalid-email", null);

		mockMvc.perform(put("/api/v1/users/user1234")
				.with(authentication(customerAuth()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	@Test
	@DisplayName("사용자 정보 수정 실패 - 서비스에서 ACCESS_DENIED 발생 시 403을 반환한다")
	void updateUser_accessDenied_returns403() throws Exception {
		willThrow(new CustomException(ErrorCode.ACCESS_DENIED))
			.given(userService).updateUser(any(), any(), any());
		UserUpdateRequestDto request = new UserUpdateRequestDto("닉네임", null, null);

		mockMvc.perform(put("/api/v1/users/user1234")
				.with(authentication(customerAuth()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
	}

	// ─── PATCH /api/v1/users/{username} ──────────────────────────────────────

	@Test
	@DisplayName("사용자 삭제 성공 - 인증된 사용자가 200을 반환한다")
	void deleteUser_authenticated_returns200() throws Exception {
		mockMvc.perform(delete("/api/v1/users/user1234")
				.with(authentication(customerAuth())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("SUCCESS"));
	}

	@Test
	@DisplayName("사용자 삭제 실패 - 미인증 요청 시 401을 반환한다")
	void deleteUser_unauthenticated_returns401() throws Exception {
		mockMvc.perform(patch("/api/v1/users/user1234"))
			.andExpect(status().isUnauthorized());
	}

	// ─── PUT /api/v1/users/{username}/role ───────────────────────────────────

	@Test
	@DisplayName("권한 변경 성공 - MASTER 권한으로 200을 반환한다")
	void updateUserRole_master_returns200() throws Exception {
		UserRoleUpdateRequestDto request = new UserRoleUpdateRequestDto(UserRole.OWNER);

		mockMvc.perform(put("/api/v1/users/user1234/role")
				.with(authentication(masterAuth()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("SUCCESS"));
	}

	@Test
	@DisplayName("권한 변경 실패 - 미인증 요청 시 401을 반환한다")
	void updateUserRole_unauthenticated_returns401() throws Exception {
		UserRoleUpdateRequestDto request = new UserRoleUpdateRequestDto(UserRole.OWNER);

		mockMvc.perform(put("/api/v1/users/user1234/role")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("권한 변경 실패 - CUSTOMER 권한으로 요청 시 403을 반환한다")
	void updateUserRole_customer_returns403() throws Exception {
		UserRoleUpdateRequestDto request = new UserRoleUpdateRequestDto(UserRole.OWNER);

		mockMvc.perform(put("/api/v1/users/user1234/role")
				.with(authentication(customerAuth()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden());
	}

	// ─── PATCH /api/v1/users/{username}/password ─────────────────────────────

	@Test
	@DisplayName("비밀번호 변경 성공 - 인증된 사용자가 200을 반환한다")
	void updatePassword_authenticated_returns200() throws Exception {
		PasswordChangeRequestDto request = new PasswordChangeRequestDto("CurrentPass1!", "NewPass1@");

		mockMvc.perform(patch("/api/v1/users/user1234/password")
				.with(authentication(customerAuth()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("SUCCESS"));
	}

	@Test
	@DisplayName("비밀번호 변경 실패 - 미인증 요청 시 401을 반환한다")
	void updatePassword_unauthenticated_returns401() throws Exception {
		PasswordChangeRequestDto request = new PasswordChangeRequestDto("CurrentPass1!", "NewPass1@");

		mockMvc.perform(patch("/api/v1/users/user1234/password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("비밀번호 변경 실패 - 현재 비밀번호 미입력 시 400과 VALIDATION_ERROR 코드를 반환한다")
	void updatePassword_blankPassword_returns400() throws Exception {
		PasswordChangeRequestDto request = new PasswordChangeRequestDto("", "NewPass1@");

		mockMvc.perform(patch("/api/v1/users/user1234/password")
				.with(authentication(customerAuth()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}
}
