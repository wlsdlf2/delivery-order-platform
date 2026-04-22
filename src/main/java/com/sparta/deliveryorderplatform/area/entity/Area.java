package com.sparta.deliveryorderplatform.area.entity;

import com.sparta.deliveryorderplatform.global.entity.BaseAuditEntity;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "p_area")
@Entity
public class Area extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "area_id", columnDefinition = "uuid")
    private UUID id;

    // 지역명(ex.광화문)
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    //  시 or 도
    @Column(nullable = false, length = 50)
    private String city;

    // 구 or 군
    @Column(nullable = false, length = 50)
    private String district;

    // 운영 활성화 여부
    @Builder.Default
    private Boolean isActive = true;

    public static Area create(String name, String city, String district) {
        if (name == null || city == null || district == null) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }

        return Area.builder()
            .name(name)
            .city(city)
            .district(district)
            .build();
    }

    // 지역 기본 정보 수정
    public void updateArea(String name, String city, String district) {
        if (name == null || city == null || district == null) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }

        this.name = name;
        this.city = city;
        this.district = district;
    }

    // 운영 지역 활성화/비활성화 스위칭
    public void updateActiveStatus(boolean isActive) {
        this.isActive = isActive;
    }

    // 운영 지역 삭제
    public void deleteArea(String username) {
        super.softDelete(username);
        this.isActive = false;
    }

}
