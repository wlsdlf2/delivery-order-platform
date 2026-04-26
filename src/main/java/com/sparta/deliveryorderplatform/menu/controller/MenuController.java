package com.sparta.deliveryorderplatform.menu.controller;

import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import com.sparta.deliveryorderplatform.menu.dto.MenuRequestDto;
import com.sparta.deliveryorderplatform.menu.dto.MenuResponseDto;
import com.sparta.deliveryorderplatform.menu.service.MenuService;
import com.sparta.deliveryorderplatform.user.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MenuController {

    private final MenuService menuService;

    @PostMapping("/stores/{storeId}/menus")
    public ResponseEntity<?> createMenu(@PathVariable UUID storeId,
                                        @Valid @RequestBody MenuRequestDto requestDto,
                                        Authentication authentication,
                                        @RequestHeader("Authorization") String authHeader) {

        //내부 client 호출을 위한 토큰 전달
        String token = authHeader.substring(7);

        menuService.createMenu(requestDto, storeId, authentication, token);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/menus/{menuId}")
    public ResponseEntity<?> getMenu(@PathVariable UUID menuId,
                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponse.success(menuService.getMenu(menuId, userDetails)));
    }

    @GetMapping("/stores/{storeId}/menus")
    public ResponseEntity<?> getMenus(@PathVariable UUID storeId,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(defaultValue = "createdAt") String sortField,
                                      @RequestParam(defaultValue = "DESC") String sortDirection,
                                      @AuthenticationPrincipal UserDetailsImpl userDetails,
                                      @PageableDefault(size = 10, page = 0) Pageable pageable) {

        Page<MenuResponseDto> menuResponseDto = menuService.getMenuList(storeId, keyword, sortField, sortDirection, userDetails, pageable);
        return ResponseEntity.ok(ApiResponse.success(menuResponseDto));
    }

    @PreAuthorize("hasRole('MASTER') or hasRole('OWNER')")
    @PutMapping("/menus/{menuId}")
    public ResponseEntity<?> updateMenu(@PathVariable UUID menuId,
                                        @Valid @RequestBody MenuRequestDto menuRequestDto,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        menuService.updateMenu(menuId, menuRequestDto, userDetails);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PreAuthorize("hasRole('MASTER') or hasRole('OWNER')")
    @DeleteMapping("/menus/{menuId}")
    public ResponseEntity<?> deleteMenu(@PathVariable UUID menuId,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        menuService.deleteMenu(menuId, userDetails);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PreAuthorize("hasRole('MASTER') or hasRole('OWNER')")
    @PatchMapping("/menus/{menuId}/hide")
    public ResponseEntity<?> patchMenuStatus(@PathVariable UUID menuId,
                                             @RequestParam Boolean isHidden,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails) {
        menuService.patchMenuStatus(menuId, isHidden, userDetails);
        return ResponseEntity.ok(ApiResponse.success());
    }
}