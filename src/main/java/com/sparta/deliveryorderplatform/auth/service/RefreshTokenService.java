package com.sparta.deliveryorderplatform.auth.service;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	private static final String PREFIX = "auth:refresh:";

	private final RedisTemplate<String, String> redisTemplate;

	public void save(String username, String token, long expirationMillis) {
		redisTemplate.opsForValue().set(PREFIX + username, token, Duration.ofMillis(expirationMillis));
	}

	public Optional<String> find(String username) {
		return Optional.ofNullable(redisTemplate.opsForValue().get(PREFIX + username));
	}

	public void delete(String username) {
		redisTemplate.delete(PREFIX + username);
	}

	public boolean validate(String username, String token) {
		return find(username).map(stored -> stored.equals(token)).orElse(false);
	}
}
