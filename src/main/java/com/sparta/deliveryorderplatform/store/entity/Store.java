package com.sparta.deliveryorderplatform.store.entity;

import com.sparta.deliveryorderplatform.area.entity.Area;
import com.sparta.deliveryorderplatform.category.entity.Category;
import com.sparta.deliveryorderplatform.global.entity.BaseAuditEntity;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "p_store")
@Entity
public class Store extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "store_id", columnDefinition = "uuid")
    private UUID id;

    // 연관관계 매핑: 가게 소유자 (User 엔티티의 username 필드와 매핑)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", referencedColumnName = "username", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

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
    @Builder.Default
    private Double averageRating = 0.0;

    @Builder.Default
    private Boolean isHidden = false;

    public static Store create(String name, String address, String phone, User owner, Category category, Area area) {
        validateStore(name, address, owner, category, area);

        return Store.builder()
                .name(name)
                .address(address)
                .phone(phone)
                .owner(owner)
                .category(category)
                .area(area)
                .build();
    }

    public void update(String name, String address, String phone, Category category, Area area, Boolean isHidden) {
        validateStore(name, address, owner, category, area);

        this.name = name;
        this.address = address;
        this.phone = phone;
        this.category = category;
        this.area = area;
        if (isHidden != null) {
            this.isHidden = isHidden;
        }
    }

    public void updateVisibility(Boolean isHidden) {
        if (isHidden != null) {
            this.isHidden = isHidden;
        }
    }

    public void updateAverageRating(Double newAvg) {
        // null이면 0.0으로, 아니면 반올림 계산
        double value = (newAvg == null) ? 0.0 : newAvg;
        this.averageRating = Math.round(value * 10) / 10.0;
    }

    public void delete(String username) {
        super.softDelete(username);
        this.isHidden = true;   // 가게 삭제 시 숨김 처리
    }

    private static void validateStore(String name, String address, User owner, Category category, Area area) {
        if (!StringUtils.hasText(name) || !StringUtils.hasText(address)) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }
        if (category == null || area == null || owner == null) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }
    }
}
