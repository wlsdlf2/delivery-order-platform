package com.sparta.deliveryorderplatform.category.entity;

import com.sparta.deliveryorderplatform.global.entity.BaseAuditEntity;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "p_category")
@Entity
public class Category extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "category_id", columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    public static Category create(String name) {
        if (name == null || name.isBlank()) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }

        return Category.builder()
            .name(name)
            .build();
    }

    public void update(String name) {
        if (name == null || name.isBlank()) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }

        this.name = name;
    }

    public void delete(String username) {
        super.softDelete(username);
    }
}
