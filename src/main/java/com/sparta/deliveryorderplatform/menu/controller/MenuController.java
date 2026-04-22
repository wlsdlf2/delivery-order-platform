package com.sparta.deliveryorderplatform.menu.controller;

import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import com.sparta.deliveryorderplatform.menu.dto.MenuResponseDto;
import com.sparta.deliveryorderplatform.menu.service.MenuServcie;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<MenuResponseDto> getMenu(@PathVariable UUID menuId) {
        return ResponseEntity.ok(menuServcie.getMenu(menuId));
    }

    @GetMapping("/stores/{storeId}/menus")
    public ResponseEntity<List<MenuResponseDto>> getMenus(@PathVariable UUID storeId) {
        List<MenuResponseDto> menuList= menuServcie.getMenuList(storeId);
        return ResponseEntity.ok(ApiResponse.success(menuList).getData());
    }
}