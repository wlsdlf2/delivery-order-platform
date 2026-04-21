package com.sparta.deliveryorderplatform.global.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

	@Bean
	public AuditorAware<Long> auditorProvider() {
		// 추후 JWT 구현 후 로그인 사용자ID가 들어가도록 수정할 예정
		return () -> Optional.of(1L);
	}
}
