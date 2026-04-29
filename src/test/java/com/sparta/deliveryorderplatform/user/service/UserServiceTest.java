package com.sparta.deliveryorderplatform.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock UserRepository userRepository;
	@Mock BCryptPasswordEncoder passwordEncoder;
	@Mock UserCacheService userCacheService;
	@InjectMocks UserService userService;

	private User createCustomer(String username) {
		return User.createUser(username, "닉네임", username + "@example.com", "encodedPassword", UserRole.CUSTOMER);
	}

	private User createMaster() {
		return User.createUser("master1", "관리자", "master@example.com", "encodedPassword", UserRole.MASTER);
	}

	// ─── getUsers ────────────────────────────────────────────────────────────

	@Test
	@DisplayName("사용자 목록 조회 성공 - 검색 조건에 맞는 페이징된 결과를 반환한다")
	void getUsers_success_returnsPageOfUsers() {
		User user = createCustomer("user1234");
		given(userRepository.searchUsers(any(), any())).willReturn(new PageImpl<>(List.of(user)));

		Page<UserResponseDto> result = userService.getUsers(
			new UserSearchCondition(null, null),
			PageRequest.of(0, 10)
		);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).username()).isEqualTo("user1234");
	}

	// ─── getUser ─────────────────────────────────────────────────────────────

	@Test
	@DisplayName("사용자 상세 조회 성공 - 본인이 조회한다")
	void getUser_self_returnsUserResponse() {
		User user = createCustomer("user1234");
		given(userRepository.findById("user1234")).willReturn(Optional.of(user));

		UserResponseDto result = userService.getUser("user1234", user);

		assertThat(result.username()).isEqualTo("user1234");
		assertThat(result.role()).isEqualTo(UserRole.CUSTOMER);
	}

	@Test
	@DisplayName("사용자 상세 조회 성공 - MASTER가 타인을 조회한다")
	void getUser_master_returnsUserResponse() {
		User master = createMaster();
		User target = createCustomer("user1234");
		given(userRepository.findById("user1234")).willReturn(Optional.of(target));

		UserResponseDto result = userService.getUser("user1234", master);

		assertThat(result.username()).isEqualTo("user1234");
	}

	@Test
	@DisplayName("사용자 상세 조회 실패 - 타인 조회 시 ACCESS_DENIED 예외 발생")
	void getUser_otherUser_throwsAccessDenied() {
		User other = createCustomer("other999");

		assertThatThrownBy(() -> userService.getUser("user1234", other))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ACCESS_DENIED);
	}

	@Test
	@DisplayName("사용자 상세 조회 실패 - 존재하지 않는 사용자 시 USER_NOT_FOUND 예외 발생")
	void getUser_notFound_throwsUserNotFound() {
		User user = createCustomer("user1234");
		given(userRepository.findById("user1234")).willReturn(Optional.empty());

		assertThatThrownBy(() -> userService.getUser("user1234", user))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_FOUND);
	}

	// ─── updateUser ──────────────────────────────────────────────────────────

	@Test
	@DisplayName("사용자 정보 수정 성공 - 본인이 닉네임을 수정한다")
	void updateUser_self_returnsUpdatedUser() {
		User user = createCustomer("user1234");
		given(userRepository.findById("user1234")).willReturn(Optional.of(user));

		UserResponseDto result = userService.updateUser(
			"user1234", new UserUpdateRequestDto("새닉네임", null, null), user
		);

		assertThat(result.nickname()).isEqualTo("새닉네임");
	}

	@Test
	@DisplayName("사용자 정보 수정 성공 - MASTER가 타인의 정보를 수정한다")
	void updateUser_master_returnsUpdatedUser() {
		User master = createMaster();
		User target = createCustomer("user1234");
		given(userRepository.findById("user1234")).willReturn(Optional.of(target));

		UserResponseDto result = userService.updateUser(
			"user1234", new UserUpdateRequestDto("새닉네임", null, null), master
		);

		assertThat(result.nickname()).isEqualTo("새닉네임");
	}

	@Test
	@DisplayName("사용자 정보 수정 실패 - 타인 수정 시 ACCESS_DENIED 예외 발생")
	void updateUser_otherUser_throwsAccessDenied() {
		User other = createCustomer("other999");

		assertThatThrownBy(() -> userService.updateUser(
			"user1234", new UserUpdateRequestDto("닉네임", null, null), other
		))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ACCESS_DENIED);
	}

	@Test
	@DisplayName("사용자 정보 수정 실패 - 존재하지 않는 사용자 시 USER_NOT_FOUND 예외 발생")
	void updateUser_notFound_throwsUserNotFound() {
		User user = createCustomer("user1234");
		given(userRepository.findById("user1234")).willReturn(Optional.empty());

		assertThatThrownBy(() -> userService.updateUser(
			"user1234", new UserUpdateRequestDto("닉네임", null, null), user
		))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_FOUND);
	}

	// ─── updatePassword ──────────────────────────────────────────────────────

	@Test
	@DisplayName("비밀번호 변경 성공 - 본인이 현재 비밀번호를 맞게 입력한다")
	void updatePassword_self_success() {
		User user = createCustomer("user1234");
		given(userRepository.findById("user1234")).willReturn(Optional.of(user));
		given(passwordEncoder.matches("CurrentPass1!", "encodedPassword")).willReturn(true);

		assertThatCode(() -> userService.updatePassword(
			"user1234", new PasswordChangeRequestDto("CurrentPass1!", "NewPass1@"), user
		)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("비밀번호 변경 실패 - 타인이 변경 시도 시 ACCESS_DENIED 예외 발생")
	void updatePassword_otherUser_throwsAccessDenied() {
		User other = createCustomer("other999");

		assertThatThrownBy(() -> userService.updatePassword(
			"user1234", new PasswordChangeRequestDto("CurrentPass1!", "NewPass1@"), other
		))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ACCESS_DENIED);
	}

	@Test
	@DisplayName("비밀번호 변경 실패 - MASTER도 타인 비밀번호 변경 불가 (ACCESS_DENIED 예외 발생)")
	void updatePassword_master_throwsAccessDenied() {
		User master = createMaster();

		assertThatThrownBy(() -> userService.updatePassword(
			"user1234", new PasswordChangeRequestDto("CurrentPass1!", "NewPass1@"), master
		))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ACCESS_DENIED);
	}

	@Test
	@DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치 시 INVALID_PASSWORD 예외 발생")
	void updatePassword_wrongCurrentPassword_throwsInvalidPassword() {
		User user = createCustomer("user1234");
		given(userRepository.findById("user1234")).willReturn(Optional.of(user));
		given(passwordEncoder.matches("WrongPass1!", "encodedPassword")).willReturn(false);

		assertThatThrownBy(() -> userService.updatePassword(
			"user1234", new PasswordChangeRequestDto("WrongPass1!", "NewPass1@"), user
		))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.INVALID_PASSWORD);
	}

	@Test
	@DisplayName("비밀번호 변경 실패 - 존재하지 않는 사용자 시 USER_NOT_FOUND 예외 발생")
	void updatePassword_notFound_throwsUserNotFound() {
		User user = createCustomer("user1234");
		given(userRepository.findById("user1234")).willReturn(Optional.empty());

		assertThatThrownBy(() -> userService.updatePassword(
			"user1234", new PasswordChangeRequestDto("CurrentPass1!", "NewPass1@"), user
		))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_FOUND);
	}

	// ─── updateUserRole ──────────────────────────────────────────────────────

	@Test
	@DisplayName("권한 변경 성공 - MASTER가 타인의 권한을 변경한다")
	void updateUserRole_master_success() {
		User master = createMaster();
		User target = createCustomer("user1234");
		given(userRepository.findById("user1234")).willReturn(Optional.of(target));

		userService.updateUserRole("user1234", new UserRoleUpdateRequestDto(UserRole.OWNER), master);

		assertThat(target.getRole()).isEqualTo(UserRole.OWNER);
	}

	@Test
	@DisplayName("권한 변경 실패 - CUSTOMER가 시도 시 ACCESS_DENIED 예외 발생")
	void updateUserRole_customer_throwsAccessDenied() {
		User customer = createCustomer("user1234");

		assertThatThrownBy(() -> userService.updateUserRole(
			"other999", new UserRoleUpdateRequestDto(UserRole.OWNER), customer
		))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ACCESS_DENIED);
	}

	@Test
	@DisplayName("권한 변경 실패 - MASTER가 자기 자신 권한 변경 시도 시 ACCESS_DENIED 예외 발생")
	void updateUserRole_masterSelf_throwsAccessDenied() {
		User master = createMaster();
		given(userRepository.findById("master1")).willReturn(Optional.of(master));

		assertThatThrownBy(() -> userService.updateUserRole(
			"master1", new UserRoleUpdateRequestDto(UserRole.CUSTOMER), master
		))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ACCESS_DENIED);
	}

	@Test
	@DisplayName("권한 변경 실패 - 존재하지 않는 사용자 시 USER_NOT_FOUND 예외 발생")
	void updateUserRole_notFound_throwsUserNotFound() {
		User master = createMaster();
		given(userRepository.findById("user1234")).willReturn(Optional.empty());

		assertThatThrownBy(() -> userService.updateUserRole(
			"user1234", new UserRoleUpdateRequestDto(UserRole.OWNER), master
		))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_FOUND);
	}

	// ─── deleteUser ──────────────────────────────────────────────────────────

	@Test
	@DisplayName("사용자 삭제 성공 - 본인이 소프트 삭제되고 deletedAt이 설정된다")
	void deleteUser_self_softDeletesUser() {
		User user = createCustomer("user1234");
		given(userRepository.findById("user1234")).willReturn(Optional.of(user));

		userService.deleteUser("user1234", user);

		assertThat(user.getDeletedAt()).isNotNull();
		assertThat(user.getDeletedBy()).isEqualTo("user1234");
	}

	@Test
	@DisplayName("사용자 삭제 성공 - MASTER가 타인을 소프트 삭제하고 deletedBy에 MASTER 아이디가 설정된다")
	void deleteUser_master_softDeletesUser() {
		User master = createMaster();
		User target = createCustomer("user1234");
		given(userRepository.findById("user1234")).willReturn(Optional.of(target));

		userService.deleteUser("user1234", master);

		assertThat(target.getDeletedAt()).isNotNull();
		assertThat(target.getDeletedBy()).isEqualTo("master1");
	}

	@Test
	@DisplayName("사용자 삭제 실패 - 타인 삭제 시도 시 ACCESS_DENIED 예외 발생")
	void deleteUser_otherUser_throwsAccessDenied() {
		User other = createCustomer("other999");

		assertThatThrownBy(() -> userService.deleteUser("user1234", other))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ACCESS_DENIED);
	}

	@Test
	@DisplayName("사용자 삭제 실패 - 존재하지 않는 사용자 시 USER_NOT_FOUND 예외 발생")
	void deleteUser_notFound_throwsUserNotFound() {
		User user = createCustomer("user1234");
		given(userRepository.findById("user1234")).willReturn(Optional.empty());

		assertThatThrownBy(() -> userService.deleteUser("user1234", user))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_FOUND);
	}
}
