package com.sparta.deliveryorderplatform.category.repository;

import com.sparta.deliveryorderplatform.category.dto.CategorySearchDTO;
import com.sparta.deliveryorderplatform.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryRepositoryCustom {
    Page<Category> searchCategoriesForAdmin(CategorySearchDTO searchDTO, Pageable pageable);
    List<Category> searchCategoriesForUser(CategorySearchDTO searchDTO);
}
