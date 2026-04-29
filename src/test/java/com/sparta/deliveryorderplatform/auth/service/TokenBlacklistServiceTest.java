package com.sparta.deliveryorderplatform.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;

import java.time.Duration;

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
class TokenBlacklistServiceTest {

	@Mock RedisTemplate<String, String> redisTemplate;
	@Mock ValueOperations<String, String> valueOperations;
	@InjectMocks TokenBlacklistService tokenBlacklistService;

	@BeforeEach
	void setUp() {
		lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
	}

	@Test
	@DisplayName("블랙리스트 등록 성공 - 유효한 TTL이면 Redis에 저장된다")
	void blacklist_validMillis_savesToRedis() {
		tokenBlacklistService.blacklist("myToken", "user1234", 300000L);

		then(valueOperations).should().set(
			argThat(key -> key.startsWith("auth:blacklist:") && key.length() == "auth:blacklist:".length() + 64),
			eq("user1234"),
			eq(Duration.ofMillis(300000L))
		);
	}

	@Test
	@DisplayName("블랙리스트 등록 스킵 - remainingMillis가 0이면 Redis 저장 안 한다")
	void blacklist_zeroMillis_doesNotSave() {
		tokenBlacklistService.blacklist("myToken", "user1234", 0L);

		then(valueOperations).should(never()).set(any(), any(), any(Duration.class));
	}

	@Test
	@DisplayName("블랙리스트 등록 스킵 - remainingMillis가 음수이면 Redis 저장 안 한다")
	void blacklist_negativeMillis_doesNotSave() {
		tokenBlacklistService.blacklist("myToken", "user1234", -1L);

		then(valueOperations).should(never()).set(any(), any(), any(Duration.class));
	}

	@Test
	@DisplayName("블랙리스트 확인 - 키가 존재하면 true를 반환한다")
	void isBlacklisted_keyExists_returnsTrue() {
		given(redisTemplate.hasKey(any())).willReturn(true);

		assertThat(tokenBlacklistService.isBlacklisted("myToken")).isTrue();
	}

	@Test
	@DisplayName("블랙리스트 확인 - 키가 없으면 false를 반환한다")
	void isBlacklisted_keyNotExists_returnsFalse() {
		given(redisTemplate.hasKey(any())).willReturn(false);

		assertThat(tokenBlacklistService.isBlacklisted("myToken")).isFalse();
	}

	@Test
	@DisplayName("블랙리스트 확인 - 같은 토큰은 항상 같은 키로 조회된다")
	void isBlacklisted_sameToken_usesSameKey() {
		given(redisTemplate.hasKey(any())).willReturn(false);

		tokenBlacklistService.isBlacklisted("myToken");
		tokenBlacklistService.isBlacklisted("myToken");

		// 같은 토큰 → 같은 해시 키 → 동일 키로 두 번 호출
		then(redisTemplate).should(org.mockito.Mockito.times(2)).hasKey(
			argThat(key -> key.startsWith("auth:blacklist:"))
		);
	}
}
