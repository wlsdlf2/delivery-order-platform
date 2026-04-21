package com.sparta.deliveryorderplatform.category.service;

import com.sparta.deliveryorderplatform.category.dto.CategoryRequestDTO;
import com.sparta.deliveryorderplatform.category.dto.CategoryResponseDTO;
import com.sparta.deliveryorderplatform.category.entity.Category;
import com.sparta.deliveryorderplatform.category.repository.CategoryRepository;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    public Page<CategoryResponseDTO> getCategories(String keyword, String role, Pageable pageable) {
        // todo 회원 권한 체크 -> 추후 수정 예정
        boolean isAdmin = "MASTER".equals(role);
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        // fetch -> 데이터 가져오기
        Page<Category> categoryPage = fetchCategories(keyword, isAdmin, hasKeyword, pageable);

        // entity -> dto(리턴)
        return categoryPage.map(category -> CategoryResponseDTO.form(category));
    }

    // todo categoryId 1건 상세조회
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
    public void deleteCategory(UUID categoryId, String deletedBy) {
        Category category = findActiveCategory(categoryId);
        category.deleteCategory(deletedBy);
    }

    // 공통 유효성 검증 및 조회
    private Category findActiveCategory(UUID categoryId) {
        return categoryRepository.findById(categoryId)
            .filter(c -> c.getDeletedAt() == null)
            .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    // 데이터 조회
    private Page<Category> fetchCategories(String keyword, boolean isAdmin, boolean hasKeyword, Pageable pageable) {
        if (isAdmin) {
            return hasKeyword
                ? categoryRepository.findAllByNameContaining(keyword, pageable)
                : categoryRepository.findAll(pageable);
        } else {
            return hasKeyword
                ? categoryRepository.findAllByDeletedAtIsNullAndNameContaining(keyword, pageable)
                : categoryRepository.findAllByDeletedAtIsNull(pageable);

        }
    }

}
