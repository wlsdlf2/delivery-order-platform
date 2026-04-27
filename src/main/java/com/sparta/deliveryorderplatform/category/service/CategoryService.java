package com.sparta.deliveryorderplatform.category.service;

import com.sparta.deliveryorderplatform.category.dto.CategoryRequestDTO;
import com.sparta.deliveryorderplatform.category.dto.CategoryResponseDTO;
import com.sparta.deliveryorderplatform.category.dto.CategorySearchDTO;
import com.sparta.deliveryorderplatform.category.entity.Category;
import com.sparta.deliveryorderplatform.category.repository.CategoryRepository;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.store.repository.StoreRepository;
import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;

    //Create
    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO requestDTO) {
        // 중복 체크
        validateDuplicateName(requestDTO.getName());

        Category category = Category.create(requestDTO.getName());
        Category savedCategory = categoryRepository.save(category);
        return CategoryResponseDTO.from(savedCategory);
    }

    //read
    @Transactional(readOnly = true)
    public Page<CategoryResponseDTO> getCategories(CategorySearchDTO searchDTO, UserRole role, Pageable pageable) {
        // 권한에 따라 검색 조건 결정 : 삭제된 데이터 조회 여부
        boolean isAdmin = role == UserRole.MASTER;

        if (isAdmin) {
            // 관리자 -> 삭제 데이터 호출 & 페이징 처리
            return categoryRepository.searchCategoriesForAdmin(searchDTO, pageable)
                    .map(CategoryResponseDTO::from);
        } else {
            // 일반 사용자 -> 삭제되지 않은 전체 목록
            List<Category> categories = categoryRepository.searchCategoriesForUser(searchDTO);
            return new PageImpl<>(
                categories.stream().map(CategoryResponseDTO::from).toList(),
                Pageable.unpaged(),
                categories.size()
            );
        }
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(UUID categoryId) {
        Category category = findCategoryById(categoryId);
        return CategoryResponseDTO.from(category);
    }

    //update
    @Transactional
    public CategoryResponseDTO updateCategory(UUID categoryId, CategoryRequestDTO requestDTO) {
        Category category = findCategoryById(categoryId);

        // 이름이 변경될 때만 중복 체크
        if (!category.getName().equals(requestDTO.getName())) {
            validateDuplicateName(requestDTO.getName());
        }

        category.update(requestDTO.getName());
        return CategoryResponseDTO.from(category);
    }

    //delete
    @Transactional
    public CategoryResponseDTO deleteCategory(UUID categoryId, User user) {
        Category category = findCategoryById(categoryId);

        // 삭제 전 연관된 가게가 있는지 확인: deletedAt만 판단
        if (storeRepository.existsByCategoryIdAndDeletedAtIsNull(categoryId)) {
            throw new CustomException(ErrorCode.EXIST_LINKED_STORES);
        }

        category.delete(user.getUsername());
        return CategoryResponseDTO.from(category);
    }

    // 헬퍼 메서드: 삭제되지 않은 데이터 조회
    @Transactional(readOnly = true)
    public Category findCategoryById(UUID categoryId) {
        return categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
            .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    // 카테고리명 중복 조회
    private void validateDuplicateName(String name) {
        if (categoryRepository.existsByNameAndDeletedAtIsNull(name)) {
            throw new CustomException(ErrorCode.DUPLICATE_CATEGORY_NAME);
        }
    }
}
