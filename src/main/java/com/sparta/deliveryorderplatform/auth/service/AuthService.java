package com.sparta.deliveryorderplatform.auth.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.deliveryorderplatform.auth.dto.LoginRequestDto;
import com.sparta.deliveryorderplatform.auth.dto.LoginResponseDto;
import com.sparta.deliveryorderplatform.auth.dto.SignUpRequestDto;
import com.sparta.deliveryorderplatform.auth.jwt.JwtTokenProvider;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.entity.UserRole;
import com.sparta.deliveryorderplatform.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final TokenBlacklistService tokenBlacklistService;
	private final RefreshTokenService refreshTokenService;

	@Transactional
	public void signup(SignUpRequestDto requestDto) {
		// 권한 검증
		if (requestDto.role() == UserRole.MASTER) {
			throw new CustomException(ErrorCode.INVALID_ROLE_SELECTION);
		}

		// 중복 아이디 확인
		if (userRepository.existsById(requestDto.username())) {
			throw new CustomException(ErrorCode.DUPLICATE_USERNAME);
		}

		// 중복 이메일 확인
		if (userRepository.existsByEmail(requestDto.email())) {
			throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
		}

		// 비밀번호 암호화 및 유저 생성
		String encodedPassword = passwordEncoder.encode(requestDto.password());
		User user = User.createUser(
			requestDto.username(),
			requestDto.nickname(),
			requestDto.email(),
			encodedPassword,
			requestDto.role()
		);

		userRepository.save(user);
	}

	public LoginResponseDto login(LoginRequestDto requestDto) {
		// 유저 확인
		User user = userRepository.findById(requestDto.username())
			.orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));

		// 삭제된 사용자 확인
		if (user.getDeletedAt() != null) {
			throw new CustomException(ErrorCode.LOGIN_FAILED);
		}

		// 비밀번호 확인
		if (!passwordEncoder.matches(requestDto.password(), user.getPassword())) {
			throw new CustomException(ErrorCode.LOGIN_FAILED);
		}

		// 토큰 발급
		String accessToken = jwtTokenProvider.createAccessToken(user.getUsername(), user.getRole());
		String refreshToken = jwtTokenProvider.createRefreshToken(user.getUsername());
		refreshTokenService.save(user.getUsername(), refreshToken, jwtTokenProvider.getRefreshTokenExpiration());

		return LoginResponseDto.of(accessToken, refreshToken, user.getUsername(), user.getRole());
	}

	public void logout(String username, String accessToken) {
		if (accessToken != null) {
			long remainingMillis = jwtTokenProvider.getRemainingValidityMillis(accessToken);
			tokenBlacklistService.blacklist(accessToken, remainingMillis);
		}
		refreshTokenService.delete(username);
		log.info("User logged out: {}", username);
	}

	public LoginResponseDto refresh(String refreshToken) {
		if (!jwtTokenProvider.validateToken(refreshToken)) {
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}
		String username = jwtTokenProvider.getUsername(refreshToken);
		if (!refreshTokenService.validate(username, refreshToken)) {
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}
		User user = userRepository.findById(username)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		String newAccessToken = jwtTokenProvider.createAccessToken(username, user.getRole());
		return LoginResponseDto.of(newAccessToken, refreshToken, username, user.getRole());
	}
}
