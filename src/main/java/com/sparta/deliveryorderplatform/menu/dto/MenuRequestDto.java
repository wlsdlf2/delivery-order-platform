package com.sparta.deliveryorderplatform.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class MenuRequestDto {

    @NotBlank
    String name;
    @NotNull
    Integer price;
    String description;

//    TODO sunny - ai 파라미터 추가 필요
}
