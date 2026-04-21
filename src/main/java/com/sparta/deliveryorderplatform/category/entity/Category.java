package com.sparta.deliveryorderplatform.category.entity;

import com.sparta.deliveryorderplatform.category.dto.CategoryRequestDTO;
import com.sparta.deliveryorderplatform.global.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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

    public static Category createCategory(CategoryRequestDTO requestDTO, String username) {
        if (requestDTO.getName() == null || requestDTO.getName().isBlank()) {
            throw new IllegalArgumentException("카테고리명 누락");
        }

        return Category.builder()
            .name(requestDTO.getName())
            .build();
    }

    public void updateCategory(String name, String username) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("카테고리명 누락");
        }

        this.name = name;
    }

    public void deleteCategory(String username) {
        super.softDelete(username);
    }
}
