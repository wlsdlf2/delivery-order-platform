package com.sparta.deliveryorderplatform.store.entity;

import com.sparta.deliveryorderplatform.area.entity.Area;
import com.sparta.deliveryorderplatform.category.entity.Category;
import com.sparta.deliveryorderplatform.global.entity.BaseAuditEntity;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
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

    // TODO: User 엔티티 연관관계 (username 매핑)
    // 현재 임시 String으로 유지하거나 User 엔티티가 있으면 변경 필요
    @Column(name = "owner_id", nullable = false)
    private String ownerId;

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

    @Column(columnDefinition = "numeric")
    @Builder.Default
    private Double averageRating = 0.0;

    @Builder.Default
    private Boolean isHidden = false;

    public static Store create(String name, String address, String phone, String ownerId, Category category, Area area) {
        validateStore(name, address, ownerId, category, area);

        return Store.builder()
                .name(name)
                .address(address)
                .phone(phone)
                .ownerId(ownerId)
                .category(category)
                .area(area)
                .build();
    }

    public void update(String name, String address, String phone, Category category, Area area, Boolean isHidden) {
        validateStore(name, address, this.ownerId, category, area);

        this.name = name;
        this.address = address;
        this.phone = phone;
        this.category = category;
        this.area = area;
        if (isHidden != null) {
            this.isHidden = isHidden;
        }
    }

    public void delete(String username) {
        super.softDelete(username);
        this.isHidden = true;
    }

    private static void validateStore(String name, String address, String ownerId, Category category, Area area) {
        if (!StringUtils.hasText(name) || !StringUtils.hasText(address) || !StringUtils.hasText(ownerId)) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }
        if (category == null || area == null) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }
    }
}
