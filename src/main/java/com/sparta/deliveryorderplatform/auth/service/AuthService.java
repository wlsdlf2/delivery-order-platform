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

		return LoginResponseDto.of(accessToken, refreshToken, user.getUsername(), user.getRole());
	}

	@Transactional
	public void logout(String username) {
		// TODO: 추후 Redis 연결 시 리프레시 토큰 삭제 및 액세스 토큰 블랙리스트 로직 추가
		log.info("User logged out: {}", username);
	}
}
