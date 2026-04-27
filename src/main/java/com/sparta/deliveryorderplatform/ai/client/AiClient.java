package com.sparta.deliveryorderplatform.ai.client;

import com.sparta.deliveryorderplatform.ai.dto.AiRequestDto;
import com.sparta.deliveryorderplatform.ai.dto.AiResponseDto;
import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiClient {

    private final RestTemplate restTemplate;

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    public String generateDescription(String aiPrompt, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<AiRequestDto> request = new HttpEntity<>(new AiRequestDto(aiPrompt), headers);

        //exchange 로 response 받기
        ResponseEntity<ApiResponse<AiResponseDto>> response = restTemplate.exchange(
                aiServiceUrl + "/api/v1/ai/product-description",    //도메인만 환경별로 교체
                HttpMethod.POST,
                request,                                //요청
                new ParameterizedTypeReference<>() {}   //응답
        );

        //response null check
        if (response.getBody() == null || response.getBody().getData() == null) {
            throw new CustomException(ErrorCode.AI_RESPONSE_NULL);
        }

        return response.getBody().getData().getResult();
    }
}
