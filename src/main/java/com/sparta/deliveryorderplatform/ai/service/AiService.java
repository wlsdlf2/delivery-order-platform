package com.sparta.deliveryorderplatform.ai.service;

import com.sparta.deliveryorderplatform.ai.dto.AiResponseDto;
import com.sparta.deliveryorderplatform.ai.entity.Ai;
import com.sparta.deliveryorderplatform.ai.repository.AiRepository;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    private final RestTemplate restTemplate;
    private final AiRepository aiRepository;
    private final UserRepository userRepository;

    @Transactional
    public AiResponseDto generateAiDescription(String aiPrompt, String username) {
        String url = geminiApiUrl + "?key=" + geminiApiKey;

        log.info("sunny aiPrompt : {}", aiPrompt);

        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of(
                        "parts", List.of(
                                Map.of("text", "당신은 음식 전문 설명 작가입니다. 항상 한국어로 답변하고, 답변을 최대한 간결하게 50자 이하로. 불필요한 인사말이나 부연 설명 없이 본문만 작성하세요.")
                        )
                ),
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", aiPrompt)
                        ))
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        String responseBody = parseGeminiResponse(response.getBody());

        //p_ai_request_log 테이블에 요청 및 응답 값 저장
        User user = userRepository.findById(username).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Ai aiEntity = new Ai(user, aiPrompt, responseBody, "상품 설명 생성");
        aiRepository.save(aiEntity);

        return new AiResponseDto(aiPrompt, responseBody);
    }

    @SuppressWarnings("unchecked")
    private String parseGeminiResponse(Map responseBody) {
        List<Map> candidates = (List<Map>) responseBody.get("candidates");
        Map content = (Map) candidates.get(0).get("content");
        List<Map> parts = (List<Map>) content.get("parts");
        return (String) parts.get(0).get("text");
    }
}
