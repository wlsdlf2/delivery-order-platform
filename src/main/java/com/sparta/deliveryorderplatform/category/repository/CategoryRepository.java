package com.sparta.deliveryorderplatform.category.repository;

import com.sparta.deliveryorderplatform.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID>, CategoryRepositoryCustom {
    boolean existsByName(String name);
}

