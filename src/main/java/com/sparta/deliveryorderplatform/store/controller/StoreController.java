package com.sparta.deliveryorderplatform.store.controller;

import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import com.sparta.deliveryorderplatform.global.common.PageResponse;
import com.sparta.deliveryorderplatform.store.dto.StoreRequestDTO;
import com.sparta.deliveryorderplatform.store.dto.StoreResponseDTO;
import com.sparta.deliveryorderplatform.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<ApiResponse<PageResponse<StoreResponseDTO>>> getStores(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(storeService.getStores(pageable))));
    }

    // get 상세 조회
    @GetMapping("/api/v1/stores/{storeId}")
    public ResponseEntity<ApiResponse<StoreResponseDTO>> getStoreById(@PathVariable UUID storeId) {
        return ResponseEntity.ok(ApiResponse.success(storeService.getStoreById(storeId)));
    }

    // update (OWNER, MASTER 권한)
    @PutMapping("/api/v1/stores/{storeId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MASTER')")
    public ResponseEntity<ApiResponse<StoreResponseDTO>> updateStore(
            @PathVariable UUID storeId,
            @Valid @RequestBody StoreRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(ApiResponse.success(storeService.updateStore(storeId, requestDTO)));
    }

    // delete (MASTER 권한만 가능하다고 가정)
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
