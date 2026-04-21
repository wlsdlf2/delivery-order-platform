package com.sparta.deliveryorderplatform.category.repository;

import com.sparta.deliveryorderplatform.category.dto.CategorySearchDTO;
import com.sparta.deliveryorderplatform.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryRepositoryCustom {
    Page<Category> searchCategories(CategorySearchDTO searchDTO, Pageable pageable);
}
