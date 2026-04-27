package com.sparta.deliveryorderplatform.menu.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;

@Getter
public class MenuRequestDto {

    @NotBlank(message = "상품 이름은 필수입니다.")
    String name;
    @NotNull(message = "가격은 필수 입니다.")
    @Min(value = 1, message = "가격은 0 이상이어야 합니다.")
    @Max(value = Integer.MAX_VALUE, message = "가격이 최대값을 초과했습니다.")
    Integer price;
    String description;

    // ai
    Boolean aiDescription;
    @Size(max = 100, message = "프롬포트는 100자 이하여야 합니다.")
    String aiPrompt;
}
