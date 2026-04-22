package com.sparta.deliveryorderplatform.menu.controller;

import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import com.sparta.deliveryorderplatform.menu.dto.MenuRequestDto;
import com.sparta.deliveryorderplatform.menu.dto.MenuResponseDto;
import com.sparta.deliveryorderplatform.menu.service.MenuServcie;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    //TODO sunny - 권한 체크 필요
    @PutMapping("/menus/{menuId}")
    public ResponseEntity<?> updateMenu(@PathVariable UUID menuId,
                                        @Valid @RequestBody MenuRequestDto menuRequestDto) {
        menuServcie.updateMenu(menuId, menuRequestDto);
        return ResponseEntity.ok(ApiResponse.success());
    }

    //TODO sunny - 권한 체크 필요
    @DeleteMapping("/menus/{menuId}")
    public ResponseEntity<?> deleteMenu(@PathVariable UUID menuId) {
        menuServcie.deleteMenu(menuId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    //TODO sunny - 권한 체크 필요
    @PatchMapping("/menus/{menuId}/hide")
    public ResponseEntity<?> patchMenuStatus(@PathVariable UUID menuId,
                                             @RequestParam Boolean isHidden) {
        menuServcie.patchMenuStatus(menuId, isHidden);
        return ResponseEntity.ok(ApiResponse.success());
    }
}