package com.sparta.deliveryorderplatform.category.service;

import com.sparta.deliveryorderplatform.category.dto.CategoryRequestDTO;
import com.sparta.deliveryorderplatform.category.dto.CategoryResponseDTO;
import com.sparta.deliveryorderplatform.category.dto.CategorySearchDTO;
import com.sparta.deliveryorderplatform.category.entity.Category;
import com.sparta.deliveryorderplatform.category.repository.CategoryRepository;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    //create
    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO requestDTO, String username, String role) {
        // TODO: 회원 기능 담당자가 권한 체크 AOP 도입 시 아래 if문 삭제 예정
        if (!"MASTER".equals(role)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        Category category = Category.createCategory(requestDTO, username);
        Category savedCategory = categoryRepository.save(category);
        return CategoryResponseDTO.form(savedCategory);
    }


    //read
    @Transactional(readOnly = true)
    public Page<CategoryResponseDTO> getCategories(CategorySearchDTO searchDTO, Pageable pageable) {
        // 데이터 가져오기(QueryDSL 메서드 호출)
        Page<Category> categoryPage = categoryRepository.searchCategories(searchDTO, pageable);

        // entity -> dto(리턴)
        return categoryPage.map(category -> CategoryResponseDTO.form(category));
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(UUID categoryId) {
        Category category = findActiveCategory(categoryId);
        return CategoryResponseDTO.form(category);
    }

    //update
    @Transactional
    public CategoryResponseDTO updateCategory(UUID categoryId, CategoryRequestDTO requestDTO, String updatedBy) {
        Category category = findActiveCategory(categoryId);
        category.updateCategory(requestDTO.getName(), updatedBy);

        return CategoryResponseDTO.form(category);
    }

    //delete
    @Transactional
    public CategoryResponseDTO deleteCategory(UUID categoryId, String deletedBy) {
        Category category = findActiveCategory(categoryId);
        category.deleteCategory(deletedBy);

        return CategoryResponseDTO.form(category);
    }

    // 공통 유효성 검증 및 조회
    private Category findActiveCategory(UUID categoryId) {
        return categoryRepository.findById(categoryId)
            .filter(c -> c.getDeletedAt() == null)
            .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
    }

}
