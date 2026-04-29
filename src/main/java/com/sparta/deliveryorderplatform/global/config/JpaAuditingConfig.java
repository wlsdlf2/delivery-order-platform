package com.sparta.deliveryorderplatform.global.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.sparta.deliveryorderplatform.global.audit.AuditorHolder;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

	@Bean
	public AuditorAware<String> auditorProvider() {
		return () -> {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if (authentication == null ||
				!authentication.isAuthenticated() ||
				authentication instanceof AnonymousAuthenticationToken) {
				// 비인증 요청(회원가입 등)은 ThreadLocal 폴백 사용
				return Optional.ofNullable(AuditorHolder.get());
			}

			return Optional.ofNullable(authentication.getName());
		};
	}
}
