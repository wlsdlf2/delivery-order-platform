package com.sparta.deliveryorderplatform.category.repository;

import com.sparta.deliveryorderplatform.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID>, CategoryRepositoryCustom {
    Optional<Category> findByIdAndDeletedAtIsNull(UUID id);
    boolean existsByNameAndDeletedAtIsNull(String name);
}
