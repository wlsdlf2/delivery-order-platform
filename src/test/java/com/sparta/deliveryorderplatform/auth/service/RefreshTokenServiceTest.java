package com.sparta.deliveryorderplatform.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

	@Mock RedisTemplate<String, String> redisTemplate;
	@Mock ValueOperations<String, String> valueOperations;
	@InjectMocks RefreshTokenService refreshTokenService;

	@BeforeEach
	void setUp() {
		lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
	}

	// ─── save ─────────────────────────────────────────────────────────────────

	@Test
	@DisplayName("리프레시 토큰 저장 - 올바른 키/값/TTL로 Redis에 저장된다")
	void save_storesTokenWithTtl() {
		refreshTokenService.save("user1234", "refreshToken", 604800000L);

		then(valueOperations).should().set(
			"auth:refresh:user1234",
			"refreshToken",
			Duration.ofMillis(604800000L)
		);
	}

	// ─── find ─────────────────────────────────────────────────────────────────

	@Test
	@DisplayName("리프레시 토큰 조회 성공 - 저장된 토큰이 있으면 Optional로 반환된다")
	void find_tokenExists_returnsOptional() {
		given(valueOperations.get("auth:refresh:user1234")).willReturn("refreshToken");

		Optional<String> result = refreshTokenService.find("user1234");

		assertThat(result).isPresent().hasValue("refreshToken");
	}

	@Test
	@DisplayName("리프레시 토큰 조회 실패 - 저장된 토큰이 없으면 Optional.empty()를 반환한다")
	void find_tokenNotExists_returnsEmpty() {
		given(valueOperations.get("auth:refresh:user1234")).willReturn(null);

		Optional<String> result = refreshTokenService.find("user1234");

		assertThat(result).isEmpty();
	}

	// ─── delete ───────────────────────────────────────────────────────────────

	@Test
	@DisplayName("리프레시 토큰 삭제 - 해당 키가 Redis에서 삭제된다")
	void delete_removesKey() {
		refreshTokenService.delete("user1234");

		then(redisTemplate).should().delete("auth:refresh:user1234");
	}

	// ─── validate ─────────────────────────────────────────────────────────────

	@Test
	@DisplayName("리프레시 토큰 검증 성공 - 저장된 토큰과 일치하면 true를 반환한다")
	void validate_matchingToken_returnsTrue() {
		given(valueOperations.get("auth:refresh:user1234")).willReturn("refreshToken");

		assertThat(refreshTokenService.validate("user1234", "refreshToken")).isTrue();
	}

	@Test
	@DisplayName("리프레시 토큰 검증 실패 - 저장된 토큰과 다르면 false를 반환한다")
	void validate_mismatchToken_returnsFalse() {
		given(valueOperations.get("auth:refresh:user1234")).willReturn("storedToken");

		assertThat(refreshTokenService.validate("user1234", "differentToken")).isFalse();
	}

	@Test
	@DisplayName("리프레시 토큰 검증 실패 - 저장된 토큰이 없으면 false를 반환한다")
	void validate_noTokenStored_returnsFalse() {
		given(valueOperations.get("auth:refresh:user1234")).willReturn(null);

		assertThat(refreshTokenService.validate("user1234", "anyToken")).isFalse();
	}
}
