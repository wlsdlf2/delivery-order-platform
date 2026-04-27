package com.sparta.deliveryorderplatform.ai.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
//기본 생성자 강제로 생성
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
public class AiResponseDto {

    private final String prompt;
    private final String result;
}
