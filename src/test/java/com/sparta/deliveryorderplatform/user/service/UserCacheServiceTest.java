package com.sparta.deliveryorderplatform.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.entity.UserRole;

@ExtendWith(MockitoExtension.class)
class UserCacheServiceTest {

	@Mock RedisTemplate<String, String> redisTemplate;
	@Mock ValueOperations<String, String> valueOperations;

	UserCacheService userCacheService;

	@BeforeEach
	void setUp() {
		lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		userCacheService = new UserCacheService(redisTemplate, new ObjectMapper());
	}

	private User createCustomer() {
		return User.createUser("user1234", "닉네임", "test@example.com", "encodedPw", UserRole.CUSTOMER);
	}

	// ─── cache ────────────────────────────────────────────────────────────────

	@Test
	@DisplayName("유저 캐시 저장 성공 - JSON 형태로 Redis에 저장된다")
	void cache_success_savesJsonToRedis() {
		User user = createCustomer();

		userCacheService.cache(user);

		ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
		then(valueOperations).should().set(
			eq("user:cache:user1234"),
			jsonCaptor.capture(),
			eq(Duration.ofMinutes(10))
		);
		assertThat(jsonCaptor.getValue())
			.contains("user1234")
			.contains("CUSTOMER");
	}

	// ─── get ──────────────────────────────────────────────────────────────────

	@Test
	@DisplayName("유저 캐시 조회 성공 - 캐시 히트 시 User 객체가 복원된다")
	void get_cacheHit_returnsReconstructedUser() {
		String json = """
			{"username":"user1234","nickname":"닉네임","email":"test@example.com",
			 "role":"CUSTOMER","isPublic":true}
			""";
		given(valueOperations.get("user:cache:user1234")).willReturn(json);

		Optional<User> result = userCacheService.get("user1234");

		assertThat(result).isPresent();
		assertThat(result.get().getUsername()).isEqualTo("user1234");
		assertThat(result.get().getRole()).isEqualTo(UserRole.CUSTOMER);
		assertThat(result.get().getNickname()).isEqualTo("닉네임");
	}

	@Test
	@DisplayName("유저 캐시 미스 - Redis에 값이 없으면 Optional.empty()를 반환한다")
	void get_cacheMiss_returnsEmpty() {
		given(valueOperations.get("user:cache:user1234")).willReturn(null);

		Optional<User> result = userCacheService.get("user1234");

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("유저 캐시 역직렬화 실패 - 깨진 JSON이면 캐시를 삭제하고 Optional.empty()를 반환한다")
	void get_invalidJson_evictsAndReturnsEmpty() {
		given(valueOperations.get("user:cache:user1234")).willReturn("invalid-json{{{");

		Optional<User> result = userCacheService.get("user1234");

		assertThat(result).isEmpty();
		then(redisTemplate).should().delete("user:cache:user1234");
	}

	// ─── evict ────────────────────────────────────────────────────────────────

	@Test
	@DisplayName("유저 캐시 삭제 - 해당 키가 Redis에서 삭제된다")
	void evict_deletesKey() {
		userCacheService.evict("user1234");

		then(redisTemplate).should().delete("user:cache:user1234");
	}

	// ─── 왕복 테스트 (cache → get) ─────────────────────────────────────────────

	@Test
	@DisplayName("캐시 저장 후 조회 - username/role/nickname이 동일하게 복원된다")
	void cacheAndGet_roundTrip_preservesUserData() {
		User original = createCustomer();

		// cache() 호출 시 저장된 JSON을 캡처
		ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
		userCacheService.cache(original);
		then(valueOperations).should().set(any(), jsonCaptor.capture(), any(Duration.class));

		// 캡처된 JSON으로 get() 시뮬레이션
		given(valueOperations.get("user:cache:user1234")).willReturn(jsonCaptor.getValue());
		Optional<User> result = userCacheService.get("user1234");

		assertThat(result).isPresent();
		assertThat(result.get().getUsername()).isEqualTo(original.getUsername());
		assertThat(result.get().getRole()).isEqualTo(original.getRole());
		assertThat(result.get().getNickname()).isEqualTo(original.getNickname());
		assertThat(result.get().getEmail()).isEqualTo(original.getEmail());
	}
}
