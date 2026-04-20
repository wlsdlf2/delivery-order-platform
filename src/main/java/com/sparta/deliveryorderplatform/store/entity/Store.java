package com.sparta.deliveryorderplatform.store.entity;

import com.sparta.deliveryorderplatform.catrgory.entity.Category;
import jakarta.persistence.*;
import lombok.*;

import java.awt.geom.Area;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "p_store")
@Entity
public class Store {

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
    @Column(precision = 2, scale = 1)   // precision 전체 자릿수, scale 소숫점 아래 자릿수
    @Builder.Default                    // Lombok 빌더 기본값 보장(0.0 강제 주입)
    private Double averageRating = 0.0;

    @Builder.Default
    private Boolean isHidden = false;

    // Audit Fields
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;

    @PrePersist
    protected void onCraete() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 정적 팩토리 메서드
    public static Store createStore(
        String name, String address, String phone, User owner, Category category, Area area
    ) {
//        if (owner.getUsername().length() > 10) {
//            throw new IllegalArgumentException("가게소유자 ID의 길이 제한 초과(10자)");
//        }

        return Store.builder()
            .name(name)
            .address(address)
            .phone(phone)
            .owner(owner)
            .category(category)
            .area(area)
            .createdBy(owner.getUsername())
            .build();
    }

    // 가게 정보 수정
    public void updateStore(
        String name, String address, String phone, Category category, Area area, String updatedBy
    ) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.category = category;
        this.area = area;
        this.updatedBy = updatedBy;
    }

    // 가게 노출 여부 설정
    public void updateVisibility(boolean isHidden, String updatedBy) {
        this.isHidden = isHidden;
        this.updatedBy = updatedBy;
    }

    // 가게 삭제(soft delete)
    public void deleteStore(String deletedBy) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
        this.isHidden = true;   // 가게 삭제 시 숨김 처리
    }
}
