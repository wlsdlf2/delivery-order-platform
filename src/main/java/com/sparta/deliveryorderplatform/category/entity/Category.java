package com.sparta.deliveryorderplatform.category.entity;

import com.sparta.deliveryorderplatform.category.dto.CategoryRequestDTO;
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
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "category_id", columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    // Audit Fields
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static Category createCategory(CategoryRequestDTO requestDTO, String username) {
        if (requestDTO.getName() == null || requestDTO.getName().isBlank()) {
            throw new IllegalArgumentException("카테고리명 누락");
        }

        return Category.builder()
            .name(requestDTO.getName())
            .createdBy(username)
            .build();
    }

    public void updateCategory(String name, String updatedBy) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("카테고리명 누락");
        }

        this.name = name;
        this.updatedBy = updatedBy;
    }


    public void deleteCategory(String deletedBy) {
        this.deletedBy = deletedBy;
        this.deletedAt = LocalDateTime.now();
    }
}
