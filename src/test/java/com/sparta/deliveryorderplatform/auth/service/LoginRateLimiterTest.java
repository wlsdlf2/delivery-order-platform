package com.sparta.deliveryorderplatform.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
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
class LoginRateLimiterTest {

	@Mock RedisTemplate<String, String> redisTemplate;
	@Mock ValueOperations<String, String> valueOperations;
	@InjectMocks LoginRateLimiter loginRateLimiter;

	@BeforeEach
	void setUp() {
		given(redisTemplate.opsForValue()).willReturn(valueOperations);
	}

	@Test
	@DisplayName("첫 번째 요청 - count=1이면 TTL을 설정하고 true를 반환한다")
	void isAllowed_firstRequest_setsTtlAndReturnsTrue() {
		given(valueOperations.increment("ratelimit:login:1.2.3.4")).willReturn(1L);

		assertThat(loginRateLimiter.isAllowed("1.2.3.4")).isTrue();

		then(redisTemplate).should().expire(
			eq("ratelimit:login:1.2.3.4"),
			eq(Duration.ofSeconds(60))
		);
	}

	@Test
	@DisplayName("이후 요청 - count=5이면 TTL을 설정하지 않고 true를 반환한다")
	void isAllowed_withinLimit_doesNotSetTtlAndReturnsTrue() {
		given(valueOperations.increment("ratelimit:login:1.2.3.4")).willReturn(5L);

		assertThat(loginRateLimiter.isAllowed("1.2.3.4")).isTrue();

		then(redisTemplate).should(never()).expire(any(), any(Duration.class));
	}

	@Test
	@DisplayName("한계 도달 - count=10이면 true를 반환한다")
	void isAllowed_atLimit_returnsTrue() {
		given(valueOperations.increment("ratelimit:login:1.2.3.4")).willReturn(10L);

		assertThat(loginRateLimiter.isAllowed("1.2.3.4")).isTrue();
	}

	@Test
	@DisplayName("한계 초과 - count=11이면 false를 반환한다")
	void isAllowed_overLimit_returnsFalse() {
		given(valueOperations.increment("ratelimit:login:1.2.3.4")).willReturn(11L);

		assertThat(loginRateLimiter.isAllowed("1.2.3.4")).isFalse();
	}

	@Test
	@DisplayName("increment null 반환 - Redis 오류 상황에서 허용(true)으로 처리한다")
	void isAllowed_nullCount_returnsTrue() {
		given(valueOperations.increment("ratelimit:login:1.2.3.4")).willReturn(null);

		assertThat(loginRateLimiter.isAllowed("1.2.3.4")).isTrue();
	}
}
