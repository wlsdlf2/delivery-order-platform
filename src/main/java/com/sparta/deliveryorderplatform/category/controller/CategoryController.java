package com.sparta.deliveryorderplatform.category.controller;

import com.sparta.deliveryorderplatform.category.dto.CategoryRequestDTO;
import com.sparta.deliveryorderplatform.category.dto.CategoryResponseDTO;
import com.sparta.deliveryorderplatform.category.service.CategoryService;
import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    // create
    public ApiResponse<CategoryResponseDTO> createCategory(
        @RequestBody CategoryRequestDTO categoryRequestDTO,
        @RequestHeader("X-Username") String username,       // 임시
        @RequestHeader("X-Role")  String role               // 임시
    ) {
        CategoryResponseDTO responseDTO = categoryService.createCategory(categoryRequestDTO, username, role);
        return ApiResponse.success(responseDTO);
    }

    // get

    // update

    // delete
}
