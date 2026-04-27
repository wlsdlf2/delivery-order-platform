package com.sparta.deliveryorderplatform.store.dto;

import com.sparta.deliveryorderplatform.store.entity.Store;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreResponseDTO {
    private UUID id;
    private String name;
    private String address;
    private String phone;
    private UUID categoryId;
    private String categoryName;
    private UUID areaId;
    private String areaName;
    private Double averageRating;
    private Boolean isHidden;
    private LocalDateTime createdAt;
    private String createdBy;

    public static StoreResponseDTO from(Store store) {
        return StoreResponseDTO.builder()
                .id(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                .phone(store.getPhone())
                .categoryId(store.getCategory().getId())
                .categoryName(store.getCategory().getName())
                .areaId(store.getArea().getId())
                .areaName(store.getArea().getName())
                .averageRating(store.getAverageRating())
                .isHidden(store.getIsHidden())
                .createdAt(store.getCreatedAt())
                .createdBy(store.getCreatedBy())
                .build();
    }
}
