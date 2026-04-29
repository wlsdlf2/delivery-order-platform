package com.sparta.deliveryorderplatform.user.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.entity.UserRole;
import com.sparta.deliveryorderplatform.user.repository.UserRepository;
import com.sparta.deliveryorderplatform.user.service.UserCacheService;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

	@Mock UserRepository userRepository;
	@Mock UserCacheService userCacheService;
	@InjectMocks UserDetailsServiceImpl userDetailsService;

	private User createCustomer(String username) {
		return User.createUser(username, "닉네임", username + "@example.com", "encodedPassword", UserRole.CUSTOMER);
	}

	@Test
	@DisplayName("정상 사용자 로드 성공 - UserDetailsImpl을 반환한다")
	void loadUserByUsername_activeUser_returnsUserDetails() {
		User user = createCustomer("user1234");
		given(userRepository.findById("user1234")).willReturn(Optional.of(user));

		UserDetails result = userDetailsService.loadUserByUsername("user1234");

		assertThat(result.getUsername()).isEqualTo("user1234");
		assertThat(result.isEnabled()).isTrue();
	}

	@Test
	@DisplayName("소프트 삭제된 사용자 - UsernameNotFoundException 발생")
	void loadUserByUsername_deletedUser_throwsUsernameNotFoundException() {
		User user = createCustomer("user1234");
		user.softDelete("admin");
		given(userRepository.findById("user1234")).willReturn(Optional.of(user));

		assertThatThrownBy(() -> userDetailsService.loadUserByUsername("user1234"))
			.isInstanceOf(UsernameNotFoundException.class);
	}

	@Test
	@DisplayName("존재하지 않는 사용자 - UsernameNotFoundException 발생")
	void loadUserByUsername_notFound_throwsUsernameNotFoundException() {
		given(userRepository.findById("ghost123")).willReturn(Optional.empty());

		assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ghost123"))
			.isInstanceOf(UsernameNotFoundException.class);
	}
}
