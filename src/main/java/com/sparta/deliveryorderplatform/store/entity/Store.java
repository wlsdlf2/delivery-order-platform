package com.sparta.deliveryorderplatform.store.entity;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.sparta.deliveryorderplatform.area.entity.Area;
import com.sparta.deliveryorderplatform.category.entity.Category;
import com.sparta.deliveryorderplatform.global.entity.BaseAuditEntity;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.util.UUID;

@Entity
@Table(name = "p_store")
@DynamicUpdate
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE)
public class Store extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "store_id", columnDefinition = "uuid")
    private UUID id;

    // 연관관계 매핑: 가게 소유자 (User 엔티티의 username 필드와 매핑)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", referencedColumnName = "username", nullable = false)
    private User owner;

    // 연관관계 매핑: 카테고리 (Category 엔티티의 category_id 필드와 매핑)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // 연관관계 매핑: 운영지역 (Area 엔티티의 area_id 필드와 매핑)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id",  nullable = false)
    private Area area;

    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 255)
    private String address;
    @Column(length = 20)
    private String phone;

    // 캐싱 컬럼
    @Column(columnDefinition = "numeric")
    @Builder.Default                    // Lombok 빌더 기본값 보장(0.0 강제 주입)
    private Double averageRating = 0.0;

    @Builder.Default
    private Boolean isHidden = false;

    public static Store create(
        String name, String address, String phone, User owner, Category category, Area area
    ) {
        if (isInvalid(name) || isInvalid(address)) throw new CustomException(ErrorCode.VALIDATION_ERROR);
        if (owner == null || category == null || area == null)  throw new CustomException(ErrorCode.VALIDATION_ERROR);

        return Store.builder()
            .name(name)
            .address(address)
            .phone(phone)
            .owner(owner)
            .category(category)
            .area(area)
            .build();
    }

    // 가게 정보 수정
    public void update(
        String name, String address, String phone, Category category, Area area
    ) {
        if (!isInvalid(name)) this.name = name;
        if (!isInvalid(address))this.address = address;
        if (phone != null) this.phone = phone;
        if (category != null) this.category = category;
        if (area != null) this.area = area;
    }

    // 가게 노출 여부 설정
    public void updateVisibility(boolean isHidden) {
        this.isHidden = isHidden;
    }

    // 가게 삭제
    public void delete(String username) {
        super.softDelete(username);
        this.isHidden = true;   // 가게 삭제 시 숨김 처리
    }

    private static boolean isInvalid(String str) {
        return str == null || str.isBlank();
    }
}
