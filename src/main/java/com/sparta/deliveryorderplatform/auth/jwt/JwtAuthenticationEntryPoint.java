package com.sparta.deliveryorderplatform.auth.jwt;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, 
                         AuthenticationException authException) throws IOException, ServletException {
        
        // 1. 필터에서 담아준 구체적인 에러 코드를 확인합니다.
        ErrorCode errorCode = (ErrorCode) request.getAttribute("exception");

        // 2. 만약 담긴 에러 코드가 없다면 (예: 토큰 없이 접근한 경우), 기본 에러를 설정합니다.
        if (errorCode == null) {
            errorCode = ErrorCode.TOKEN_NOT_FOUND;
        }

        // 3. 공통 응답 처리
        response.setStatus(errorCode.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // GlobalExceptionHandler와 동일한 응답 포맷으로 직접 작성
        String result = objectMapper.writeValueAsString(ApiResponse.fail(errorCode));
        response.getWriter().write(result);
    }
}