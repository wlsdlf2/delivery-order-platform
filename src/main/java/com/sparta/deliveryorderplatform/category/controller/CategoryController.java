package com.sparta.deliveryorderplatform.category.controller;

import com.sparta.deliveryorderplatform.category.dto.CategoryRequestDTO;
import com.sparta.deliveryorderplatform.category.dto.CategoryResponseDTO;
import com.sparta.deliveryorderplatform.category.dto.CategorySearchDTO;
import com.sparta.deliveryorderplatform.category.service.CategoryService;
import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    // create
    @PostMapping
    public ApiResponse<CategoryResponseDTO> createCategory(
        @Valid @RequestBody CategoryRequestDTO categoryRequestDTO,
        @RequestHeader("X-Username") String username,          // 임시
        @RequestHeader("X-Role")  String role               // 임시
    ) {
        return ApiResponse.success(categoryService.createCategory(categoryRequestDTO, username, role));
    }

    // get 목록조회
    @GetMapping
    public ApiResponse<Page<CategoryResponseDTO>> getCategories(
        CategorySearchDTO searchDTO,
        @RequestHeader("X-Role") String role,
        Pageable pageable
    ) {
        return ApiResponse.success(categoryService.getCategories(searchDTO, role, pageable));
    }


    // get 1건 상세조회
    @GetMapping("/{categoryId}")
    public ApiResponse<CategoryResponseDTO> getCategoryById(@PathVariable UUID categoryId) {
        return ApiResponse.success(categoryService.getCategoryById(categoryId));
    }

    // update
    @PutMapping("/{categoryId}")
    public ApiResponse<CategoryResponseDTO> updateCategory(
        @PathVariable UUID categoryId,
        @Valid @RequestBody CategoryRequestDTO requestDTO,
        @RequestHeader("X-Username") String username,
        @RequestHeader("X-Role")  String role
    ) {
        return ApiResponse.success(categoryService.updateCategory(categoryId, requestDTO));
    }

    // delete
    @PatchMapping("/{categoryId}")
    public ApiResponse<CategoryResponseDTO> deleteCategory(
        @PathVariable UUID categoryId,
        @RequestHeader("X-Username") String username,
        @RequestHeader("X-Role")  String role
    ) {
        return ApiResponse.success(categoryService.deleteCategory(categoryId, username));
    }
}
