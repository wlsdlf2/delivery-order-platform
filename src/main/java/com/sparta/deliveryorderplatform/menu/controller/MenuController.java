package com.sparta.deliveryorderplatform.menu.controller;

import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import com.sparta.deliveryorderplatform.menu.dto.MenuResponseDto;
import com.sparta.deliveryorderplatform.menu.service.MenuServcie;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MenuController {

    private final MenuServcie menuServcie;

    @GetMapping("/menus/{menuId}")
    public ResponseEntity<?> getMenu(@PathVariable UUID menuId) {
        return ResponseEntity.ok(ApiResponse.success(menuServcie.getMenu(menuId)));
    }

    @GetMapping("/stores/{storeId}/menus")
    public ResponseEntity<?> getMenus(@PathVariable UUID storeId,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {

        Page<MenuResponseDto> menuResponseDto = menuServcie.getMenuList(storeId, page, size);
        return ResponseEntity.ok(ApiResponse.success(menuResponseDto));

    }
}