package com.sparta.deliveryorderplatform.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.user.dto.PasswordChangeRequestDto;
import com.sparta.deliveryorderplatform.user.dto.UserResponseDto;
import com.sparta.deliveryorderplatform.user.dto.UserRoleUpdateRequestDto;
import com.sparta.deliveryorderplatform.user.dto.UserSearchCondition;
import com.sparta.deliveryorderplatform.user.dto.UserUpdateRequestDto;
import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.entity.UserRole;
import com.sparta.deliveryorderplatform.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder;

	public Page<UserResponseDto> getUsers (UserSearchCondition condition, Pageable pageable) {
		Page<User> userPage = userRepository.searchUsers(condition, pageable);
		return userPage.map(UserResponseDto::from);
	}

	public UserResponseDto getUser(String username, User loginUser) {
		checkPermission(username, loginUser);
		User user = findById(username);
		return UserResponseDto.from(user);
	}

	@Transactional
	public UserResponseDto updateUser(String username, UserUpdateRequestDto requestDto, User loginUser) {
		checkPermission(username, loginUser);
		User user = findById(username);

		// 공통 수정 필드 (닉네임, 이메일, 공개여부) - 본인/MASTER 가능
		user.updateProfile(requestDto.nickname(), requestDto.email(), requestDto.isPublic());

		// 필요 시 다른 필드 업데이트 로직 추가
		return UserResponseDto.from(user);
	}

	public void updatePassword(String username, PasswordChangeRequestDto requestDto, User loginUser) {
		// 1. 본인 확인 (비밀번호 변경은 관리자도 불가능)
		if (!loginUser.getUsername().equals(username)) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}

		User user = findById(username);

		// 2. 현재 비밀번호 일치 여부 확인
		if (!passwordEncoder.matches(requestDto.currentPassword(), user.getPassword())) {
			throw new CustomException(ErrorCode.INVALID_PASSWORD); // 비밀번호 불일치 에러
		}

		// 3. 새 비밀번호 암호화 및 업데이트
		user.updatePassword(passwordEncoder.encode(requestDto.newPassword()));
	}

	public void updateUserRole (String username, UserRoleUpdateRequestDto requestDto, User loginUser) {
		if (loginUser.getRole() != UserRole.MASTER) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}

		User user = findById(username);

		if (user.getUsername().equals(loginUser.getUsername())) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}

		user.updateRole(requestDto.role());
	}

	@Transactional
	public void deleteUser(String username, User loginUser) {
		checkPermission(username, loginUser);
		User user = findById(username);

		user.softDelete(loginUser.getUsername());
	}

	// 공통 권한 체크 메서드
	private void checkPermission(String username, User loginUser) {
		if (loginUser.getRole() == UserRole.MASTER) {
			return; // 관리자면 통과
		}
		if (!loginUser.getUsername().equals(username)) {
			throw new CustomException(ErrorCode.ACCESS_DENIED); // 본인이 아니면 거부
		}
	}

	private User findById(String username) {
		return userRepository.findById(username)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
	}
}
