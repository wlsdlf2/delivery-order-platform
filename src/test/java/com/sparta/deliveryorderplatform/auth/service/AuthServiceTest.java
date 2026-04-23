package com.sparta.deliveryorderplatform.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import com.sparta.deliveryorderplatform.auth.dto.LoginRequestDto;
import com.sparta.deliveryorderplatform.auth.dto.LoginResponseDto;
import com.sparta.deliveryorderplatform.auth.dto.SignUpRequestDto;
import com.sparta.deliveryorderplatform.auth.jwt.JwtTokenProvider;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.entity.UserRole;
import com.sparta.deliveryorderplatform.user.repository.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock UserRepository userRepository;
	@Mock BCryptPasswordEncoder passwordEncoder;
	@Mock JwtTokenProvider jwtTokenProvider;
	@InjectMocks AuthService authService;

	// ─── 회원가입 ────────────────────────────────────────────────────────────

	@Test
	@DisplayName("회원가입 성공 - 유저가 저장된다")
	void signup_success() {
		SignUpRequestDto request = new SignUpRequestDto(
			"user1234", "Password1!", "닉네임", "test@example.com", UserRole.CUSTOMER
		);
		given(userRepository.existsById("user1234")).willReturn(false);
		given(userRepository.existsByEmail("test@example.com")).willReturn(false);
		given(passwordEncoder.encode("Password1!")).willReturn("encodedPassword");

		authService.signup(request);

		verify(userRepository).save(any(User.class));
	}

	@Test
	@DisplayName("회원가입 실패 - MASTER 권한 선택 시 INVALID_ROLE_SELECTION 예외 발생")
	void signup_masterRole_throwsInvalidRoleSelection() {
		SignUpRequestDto request = new SignUpRequestDto(
			"user1234", "Password1!", "닉네임", "test@example.com", UserRole.MASTER
		);

		assertThatThrownBy(() -> authService.signup(request))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.INVALID_ROLE_SELECTION);

		verify(userRepository, never()).save(any());
	}

	@Test
	@DisplayName("회원가입 실패 - 중복 아이디 시 DUPLICATE_USERNAME 예외 발생")
	void signup_duplicateUsername_throwsDuplicateUsername() {
		SignUpRequestDto request = new SignUpRequestDto(
			"user1234", "Password1!", "닉네임", "test@example.com", UserRole.CUSTOMER
		);
		given(userRepository.existsById("user1234")).willReturn(true);

		assertThatThrownBy(() -> authService.signup(request))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.DUPLICATE_USERNAME);

		verify(userRepository, never()).save(any());
	}

	@Test
	@DisplayName("회원가입 실패 - 중복 이메일 시 DUPLICATE_EMAIL 예외 발생")
	void signup_duplicateEmail_throwsDuplicateEmail() {
		SignUpRequestDto request = new SignUpRequestDto(
			"user1234", "Password1!", "닉네임", "test@example.com", UserRole.CUSTOMER
		);
		given(userRepository.existsById("user1234")).willReturn(false);
		given(userRepository.existsByEmail("test@example.com")).willReturn(true);

		assertThatThrownBy(() -> authService.signup(request))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.DUPLICATE_EMAIL);

		verify(userRepository, never()).save(any());
	}

	// ─── 로그인 ──────────────────────────────────────────────────────────────

	@Test
	@DisplayName("로그인 성공 - 액세스 토큰과 리프레시 토큰이 반환된다")
	void login_success_returnsTokens() {
		LoginRequestDto request = new LoginRequestDto("user1234", "Password1!");
		User user = User.createUser("user1234", "닉네임", "test@example.com", "encodedPassword", UserRole.CUSTOMER);

		given(userRepository.findById("user1234")).willReturn(Optional.of(user));
		given(passwordEncoder.matches("Password1!", "encodedPassword")).willReturn(true);
		given(jwtTokenProvider.createAccessToken("user1234", UserRole.CUSTOMER)).willReturn("accessToken");
		given(jwtTokenProvider.createRefreshToken("user1234")).willReturn("refreshToken");

		LoginResponseDto response = authService.login(request);

		assertThat(response.accessToken()).isEqualTo("accessToken");
		assertThat(response.refreshToken()).isEqualTo("refreshToken");
	}

	@Test
	@DisplayName("로그인 실패 - 존재하지 않는 아이디 시 LOGIN_FAILED 예외 발생")
	void login_userNotFound_throwsLoginFailed() {
		LoginRequestDto request = new LoginRequestDto("nouser1", "Password1!");
		given(userRepository.findById("nouser1")).willReturn(Optional.empty());

		assertThatThrownBy(() -> authService.login(request))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.LOGIN_FAILED);
	}

	@Test
	@DisplayName("로그인 실패 - 비밀번호 불일치 시 LOGIN_FAILED 예외 발생")
	void login_wrongPassword_throwsLoginFailed() {
		LoginRequestDto request = new LoginRequestDto("user1234", "WrongPass1!");
		User user = User.createUser("user1234", "닉네임", "test@example.com", "encodedPassword", UserRole.CUSTOMER);

		given(userRepository.findById("user1234")).willReturn(Optional.of(user));
		given(passwordEncoder.matches("WrongPass1!", "encodedPassword")).willReturn(false);

		assertThatThrownBy(() -> authService.login(request))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.LOGIN_FAILED);
	}

	// ─── 로그아웃 ─────────────────────────────────────────────────────────────

	@Test
	@DisplayName("로그아웃 성공 - 예외 없이 처리된다")
	void logout_success_noException() {
		assertThatCode(() -> authService.logout("user1234"))
			.doesNotThrowAnyException();
	}
}
