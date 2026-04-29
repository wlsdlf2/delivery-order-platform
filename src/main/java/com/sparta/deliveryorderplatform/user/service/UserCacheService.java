package com.sparta.deliveryorderplatform.user.service;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.entity.UserRole;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCacheService {

	private static final String PREFIX = "user:cache:";
	private static final Duration TTL = Duration.ofMinutes(10);

	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	public void cache(User user) {
		try {
			UserCacheData data = new UserCacheData(
				user.getUsername(), user.getNickname(), user.getEmail(),
				user.getRole(), user.getIsPublic()
			);
			redisTemplate.opsForValue().set(PREFIX + user.getUsername(),
				objectMapper.writeValueAsString(data), TTL);
		} catch (JsonProcessingException e) {
			log.warn("유저 캐시 저장 실패: {}", user.getUsername(), e);
		}
	}

	public Optional<User> get(String username) {
		String json = redisTemplate.opsForValue().get(PREFIX + username);
		if (json == null) return Optional.empty();
		try {
			UserCacheData data = objectMapper.readValue(json, UserCacheData.class);
			return Optional.of(User.reconstruct(
				data.username(), data.nickname(), data.email(),
				data.role(), data.isPublic()
			));
		} catch (Exception e) {
			log.warn("유저 캐시 역직렬화 실패, 캐시 삭제: {}", username, e);
			evict(username);
			return Optional.empty();
		}
	}

	public void evict(String username) {
		redisTemplate.delete(PREFIX + username);
	}

	private record UserCacheData(
		String username,
		String nickname,
		String email,
		UserRole role,
		Boolean isPublic
	) {}
}
