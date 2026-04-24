package com.sparta.deliveryorderplatform.store.controller;

import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import com.sparta.deliveryorderplatform.global.common.PageResponse;
import com.sparta.deliveryorderplatform.store.dto.StoreRequestDTO;
import com.sparta.deliveryorderplatform.store.dto.StoreResponseDTO;
import com.sparta.deliveryorderplatform.store.dto.StoreSearchDTO;
import com.sparta.deliveryorderplatform.store.dto.StoreVisibilityRequestDTO;
import com.sparta.deliveryorderplatform.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    // create (OWNER)
    @PostMapping("/api/v1/stores")
    @PreAuthorize("hasAnyAuthority('OWNER')")
    public ResponseEntity<ApiResponse<StoreResponseDTO>> createStore(
            @Valid @RequestBody StoreRequestDTO requestDTO,
            Authentication authentication
    ) {
        String username = authentication.getName();
        return ResponseEntity.ok(ApiResponse.success(storeService.createStore(requestDTO, username)));
    }

    // get 목록 조회
    @GetMapping("/api/v1/stores")
    public ResponseEntity<ApiResponse<PageResponse<StoreResponseDTO>>> getStores(
            StoreSearchDTO searchDTO,
            Pageable pageable,
            Authentication authentication
    ) {
        if (authentication != null) {
            searchDTO.setUsername(authentication.getName());
            searchDTO.setRole(authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst().orElse(null));
        }
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(storeService.getStores(searchDTO, pageable))));
    }

    // get 상세 조회
    @GetMapping("/api/v1/stores/{storeId}")
    public ResponseEntity<ApiResponse<StoreResponseDTO>> getStoreById(
            @PathVariable UUID storeId,
            Authentication authentication
    ) {
        String username = (authentication != null) ? authentication.getName() : null;
        String role = (authentication != null) ? authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse(null) : null;

        return ResponseEntity.ok(ApiResponse.success(storeService.getStoreById(storeId, username, role)));
    }

    // update (OWNER, MASTER 권한)
    @PutMapping("/api/v1/stores/{storeId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MASTER')")
    public ResponseEntity<ApiResponse<StoreResponseDTO>> updateStore(
            @PathVariable UUID storeId,
            @Valid @RequestBody StoreRequestDTO requestDTO,
            Authentication authentication
    ) {
        String username = authentication.getName();
        return ResponseEntity.ok(ApiResponse.success(storeService.updateStore(storeId, requestDTO, username)));
    }

    // hide 가게 숨김 여부 수정 (PATCH)
    @PatchMapping("/api/v1/stores/{storeId}/hide")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MASTER')")
    public ResponseEntity<ApiResponse<StoreResponseDTO>> updateStoreVisibility(
            @PathVariable UUID storeId,
            @Valid @RequestBody StoreVisibilityRequestDTO requestDTO,
            Authentication authentication
    ) {
        String username = authentication.getName();
        return ResponseEntity.ok(ApiResponse.success(storeService.updateVisibility(storeId, requestDTO.getIsHidden(), username)));
    }

    // delete
    @PatchMapping("/api/v1/stores/{storeId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MASTER')")
    public ResponseEntity<ApiResponse<StoreResponseDTO>> deleteStore(
            @PathVariable UUID storeId,
            Authentication authentication
    ) {
        String username = authentication.getName();
        return ResponseEntity.ok(ApiResponse.success(storeService.deleteStore(storeId, username)));
    }
}
