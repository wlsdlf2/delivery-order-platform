package com.sparta.deliveryorderplatform.user.controller;

import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import com.sparta.deliveryorderplatform.global.common.PageResponse;
import com.sparta.deliveryorderplatform.user.dto.PasswordChangeRequestDto;
import com.sparta.deliveryorderplatform.user.dto.UserResponseDto;
import com.sparta.deliveryorderplatform.user.dto.UserRoleUpdateRequestDto;
import com.sparta.deliveryorderplatform.user.dto.UserSearchCondition;
import com.sparta.deliveryorderplatform.user.dto.UserUpdateRequestDto;
import com.sparta.deliveryorderplatform.user.security.UserDetailsImpl;
import com.sparta.deliveryorderplatform.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private static final Set<Integer> ALLOWED_PAGE_SIZES = Set.of(10, 30, 50);

	private final UserService userService;

	// 사용자 목록 조회 (MASTER만 가능)
	@GetMapping
	@PreAuthorize("hasAnyRole('MASTER')")
	public ResponseEntity<ApiResponse<PageResponse<UserResponseDto>>> getUsers(
		UserSearchCondition condition,
		@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		Pageable validated = ALLOWED_PAGE_SIZES.contains(pageable.getPageSize())
			? pageable
			: PageRequest.of(pageable.getPageNumber(), 10, pageable.getSort());
		return ResponseEntity.ok(ApiResponse.success(PageResponse.of(userService.getUsers(condition, validated))));
	}

	// 사용자 상세 조회
	@GetMapping("/{username}")
	public ResponseEntity<ApiResponse<UserResponseDto>> getUser(
		@PathVariable String username,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		return ResponseEntity.ok(ApiResponse.success(userService.getUser(username,userDetails.getUser())));
	}

	// 사용자 정보 수정
	@PutMapping("/{username}")
	public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
		@PathVariable String username,
		@Valid @RequestBody UserUpdateRequestDto requestDto,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		return ResponseEntity.ok(ApiResponse.success(userService.updateUser(username, requestDto, userDetails.getUser())));
	}

	// 사용자 삭제
	@DeleteMapping("/{username}")
	public ResponseEntity<ApiResponse<Void>> deleteUser(
		@PathVariable String username,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		userService.deleteUser(username, userDetails.getUser());
		return ResponseEntity.ok(ApiResponse.success());
	}

	// 사용자 권한 수정
	@PutMapping("/{username}/role")
	@PreAuthorize("hasRole('MASTER')")
	public ResponseEntity<ApiResponse<Void>> updateUserRole(
		@PathVariable String username,
		@RequestBody UserRoleUpdateRequestDto requestDto,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		userService.updateUserRole(username, requestDto, userDetails.getUser());
		return ResponseEntity.ok(ApiResponse.success());
	}

	// 비밀번호 변경
	@PatchMapping("/{username}/password")
	public ResponseEntity<ApiResponse<Void>> updatePassword(
		@PathVariable String username,
		@RequestBody @Valid PasswordChangeRequestDto requestDto,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		userService.updatePassword(username, requestDto, userDetails.getUser());
		return ResponseEntity.ok(ApiResponse.success(null));
	}
}
