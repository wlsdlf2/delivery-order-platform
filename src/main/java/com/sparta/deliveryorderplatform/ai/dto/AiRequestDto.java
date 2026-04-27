package com.sparta.deliveryorderplatform.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class AiRequestDto {

    @NotBlank(message = "프롬포트를 입력해주세요.")
    String aiPrompt;

    public AiRequestDto(String aiPrompt) {
        this.aiPrompt = aiPrompt;
    }
}
