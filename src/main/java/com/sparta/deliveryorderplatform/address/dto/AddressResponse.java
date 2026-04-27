package com.sparta.deliveryorderplatform.address.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.sparta.deliveryorderplatform.address.entity.Address;

import lombok.Builder;

@Builder
public record AddressResponse(
    UUID id,
    String alias,
    String address,
    String detail,
    String zipCode,
    Boolean isDefault,
    LocalDateTime createdAt
) {
    // 엔티티를 DTO로 변환하는 정적 메서드
    public static AddressResponse from(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .alias(address.getAlias())
                .address(address.getAddress())
                .detail(address.getDetail())
                .zipCode(address.getZipCode())
                .isDefault(address.isDefault())
                .createdAt(address.getCreatedAt())
                .build();
    }
}