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

    //Create
    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO requestDTO, String username, String role) {
        // 중복 체크
        if (categoryRepository.existsByNameAndDeletedAtIsNull(requestDTO.getName())) {
            throw new CustomException(ErrorCode.DUPLICATE_CATEGORY_NAME);
        }

        Category category = Category.create(requestDTO.getName());
        Category savedCategory = categoryRepository.save(category);
        return CategoryResponseDTO.from(savedCategory);
    }

    //read
    @Transactional(readOnly = true)
    public Page<CategoryResponseDTO> getCategories(CategorySearchDTO searchDTO, String role, Pageable pageable) {
        // 권한에 따라 검색 조건 결정 : 삭제된 데이터 조회 여부
        searchDTO.setIsAdmin("MASTER".equals(role));

        // 데이터 가져오기(QueryDSL 메서드 호출)
        Page<Category> categoryPage = categoryRepository.searchCategories(searchDTO, pageable);

        // entity -> dto(리턴)
        return categoryPage.map(category -> CategoryResponseDTO.from(category));
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(UUID categoryId) {
        Category category = findActiveCategory(categoryId);
        return CategoryResponseDTO.from(category);
    }

    //update
    @Transactional
    public CategoryResponseDTO updateCategory(UUID categoryId, CategoryRequestDTO requestDTO) {
        Category category = findActiveCategory(categoryId);

        // 이름이 변경될 때만 중복 체크
        if (!category.getName().equals(requestDTO.getName())) {
            if (categoryRepository.existsByNameAndDeletedAtIsNull(requestDTO.getName())) {
                throw new CustomException(ErrorCode.DUPLICATE_CATEGORY_NAME);
            }
        }

        category.update(requestDTO.getName());

        return CategoryResponseDTO.from(category);
    }

    //delete
    @Transactional
    public CategoryResponseDTO deleteCategory(UUID categoryId, String username) {
        Category category = findActiveCategory(categoryId);
        category.delete(username);

        return CategoryResponseDTO.from(category);
    }

    // 공통 유효성 검증 및 조회
    private Category findActiveCategory(UUID categoryId) {
        return categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
            .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}
