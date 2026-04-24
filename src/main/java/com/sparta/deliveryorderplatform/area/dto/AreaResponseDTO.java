package com.sparta.deliveryorderplatform.area.dto;

import com.sparta.deliveryorderplatform.area.entity.Area;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AreaResponseDTO {
    private UUID id;
    private String name;
    private String city;
    private String district;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    public static AreaResponseDTO from(Area area) {
        return AreaResponseDTO.builder()
                .id(area.getId())
                .name(area.getName())
                .city(area.getCity())
                .district(area.getDistrict())
                .isActive(area.getIsActive())
                .createdAt(area.getCreatedAt())
                .createdBy(area.getCreatedBy())
                .updatedAt(area.getUpdatedAt())
                .updatedBy(area.getUpdatedBy())
                .build();
    }
}
