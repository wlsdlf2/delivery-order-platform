package com.sparta.deliveryorderplatform.address.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressUpdateRequest(
    String alias,
    @NotBlank(message = "주소는 필수 입력 사항입니다.")
    String address,
    String detail,
    String zipCode,
    boolean isDefault
) {}