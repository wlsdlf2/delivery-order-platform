package com.sparta.deliveryorderplatform.auth.service;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginRateLimiter {

	private static final String PREFIX = "ratelimit:login:";
	private static final int MAX_ATTEMPTS = 10;
	private static final long WINDOW_SECONDS = 60;

	private final RedisTemplate<String, String> redisTemplate;

	public boolean isAllowed(String clientIp) {
		String key = PREFIX + clientIp;
		Long count = redisTemplate.opsForValue().increment(key);
		if (count != null && count == 1) {
			redisTemplate.expire(key, Duration.ofSeconds(WINDOW_SECONDS));
		}
		return count == null || count <= MAX_ATTEMPTS;
	}
}
