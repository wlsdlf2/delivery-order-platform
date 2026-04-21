package com.sparta.deliveryorderplatform.area.entity;

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
public class Area {

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

    public static Area createArea(String name, String city, String district, String username) {
        if (name == null || city == null || district == null) {
            throw new IllegalArgumentException("운영지역 정보(명칭, 시/도, 구/군) 누락");
        }

        return Area.builder()
            .name(name)
            .city(city)
            .district(district)
//            .createdBy(username)
            .build();
    }

    // 지역 기본 정보 수정
    public void updateArea(String name, String city, String district, String username) {
        if (name == null || city == null || district == null) {
            throw new IllegalArgumentException("운영지역 정보(명칭, 시/도, 구/군) 누락");
        }

        this.name = name;
        this.city = city;
        this.district = district;
//        this.updatedBy = updatedBy;
    }

    // 운영 지역 활성화/비활성화 스위칭
    public void updateActiveStatus(boolean isActive, String username) {
        this.isActive = isActive;
//        this.updatedBy = updatedBy;
    }

    public void deleteArea(String username) {
        this.isActive = false;
//        this.deletedAt = LocalDateTime.now();
//        this.deletedBy = deletedBy;
    }

}
