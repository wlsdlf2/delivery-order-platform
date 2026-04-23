package com.sparta.deliveryorderplatform.menu.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

//    TODO sunny - ai 파라미터 추가 필요
}
