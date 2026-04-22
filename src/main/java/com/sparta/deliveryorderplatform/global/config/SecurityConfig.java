package com.sparta.deliveryorderplatform.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.sparta.deliveryorderplatform.auth.jwt.JwtAuthenticationEntryPoint;
import com.sparta.deliveryorderplatform.auth.jwt.JwtAuthenticationFilter;
import com.sparta.deliveryorderplatform.auth.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtTokenProvider jwtTokenProvider;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.sessionManagement( // jwt 토큰 사용
				session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/api/v1/auth/**",	// 회원가입/로그인
					"/v3/api-docs/**",	// Swagger
					"/swagger-ui/**",
					"swagger-ui.html"
				).permitAll()
				.anyRequest().authenticated() // 그 외는 모두 토큰이 있어야 함.
			)
			// jwt 예외 처리 핸들러 등록
			.exceptionHandling(exception -> exception
					.authenticationEntryPoint(jwtAuthenticationEntryPoint)
			)
			// 미리 만들어둔 JwtAuthenticationFilter를
			// UsernamePasswordAuthenticationFilter(기본 로그인 필터) 보다 먼저 실행되게 끼워 넣는다.
			.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
							UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
