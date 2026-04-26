package com.sparta.deliveryorderplatform.global.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

	@Bean
	public AuditorAware<String> auditorProvider() {
		return () -> {
			// SecurityContext에서 인증 정보 가져오기
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			// 인증 정보가 없거나 익명 사용자인 경우 처리
			if (authentication == null ||
				!authentication.isAuthenticated() ||
				authentication instanceof AnonymousAuthenticationToken) {
				return Optional.empty();
			}

			// 현재 로그인한 사용자의 username 반환
			return Optional.ofNullable(authentication.getName());
		};
	}
}
