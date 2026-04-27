package com.sparta.deliveryorderplatform.ai.controller;

import com.sparta.deliveryorderplatform.ai.dto.AiRequestDto;
import com.sparta.deliveryorderplatform.ai.dto.AiResponseDto;
import com.sparta.deliveryorderplatform.ai.service.AiService;
import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiService aiService;

    @PreAuthorize("hasRole('MASTER') or hasRole('OWNER')")
    @PostMapping("/product-description")
    public ResponseEntity<?> createAiDescription(@Valid @RequestBody AiRequestDto requestDto,
                                                 @AuthenticationPrincipal UserDetails userDetails) {

        AiResponseDto aiResponseDto = aiService.generateAiDescription(requestDto.getAiPrompt(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(aiResponseDto));
    }
}
