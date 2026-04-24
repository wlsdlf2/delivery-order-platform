package com.sparta.deliveryorderplatform.category.controller;

import com.sparta.deliveryorderplatform.category.dto.CategoryRequestDTO;
import com.sparta.deliveryorderplatform.category.dto.CategoryResponseDTO;
import com.sparta.deliveryorderplatform.category.dto.CategorySearchDTO;
import com.sparta.deliveryorderplatform.category.service.CategoryService;
import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import com.sparta.deliveryorderplatform.global.common.PageResponse;
import com.sparta.deliveryorderplatform.user.entity.UserRole;
import com.sparta.deliveryorderplatform.user.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    // create
    @PostMapping
    @PreAuthorize("hasAnyRole('MASTER')")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> createCategory(@Valid @RequestBody CategoryRequestDTO categoryRequestDTO) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.createCategory(categoryRequestDTO)));
    }

    // get 목록조회
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CategoryResponseDTO>>> getCategories(
        CategorySearchDTO searchDTO,
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        Pageable pageable
    ) {
        UserRole role = userDetails.getUser().getRole();
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(categoryService.getCategories(searchDTO, role, pageable))));
    }


    // get 1건 상세조회
    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> getCategoryById(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategoryById(categoryId)));
    }

    // update
    @PutMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('MASTER')")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> updateCategory(
        @PathVariable UUID categoryId,
        @Valid @RequestBody CategoryRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.updateCategory(categoryId, requestDTO)));
    }

    // delete
    @PatchMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('MASTER')")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> deleteCategory(
        @PathVariable UUID categoryId,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.deleteCategory(categoryId, userDetails.getUser())));
    }
}
