package com.sparta.deliveryorderplatform.store.controller;

import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import com.sparta.deliveryorderplatform.global.common.PageResponse;
import com.sparta.deliveryorderplatform.store.dto.StoreRequestDTO;
import com.sparta.deliveryorderplatform.store.dto.StoreResponseDTO;
import com.sparta.deliveryorderplatform.store.dto.StoreSearchDTO;
import com.sparta.deliveryorderplatform.store.dto.StoreVisibilityRequestDTO;
import com.sparta.deliveryorderplatform.store.service.StoreService;
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
@RequiredArgsConstructor
@RequestMapping("/api/v1/stores")
public class StoreController {

    private final StoreService storeService;

    // create (OWNER)
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER')")
    public ResponseEntity<ApiResponse<StoreResponseDTO>> createStore(
            @Valid @RequestBody StoreRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(storeService.createStore(requestDTO, userDetails.getUser())));
    }

    // get 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StoreResponseDTO>>> getStores(
            StoreSearchDTO searchDTO,
            Pageable pageable,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        // 1. 인증 정보에서 필요한 값만 추출하여 DTO에 세팅
        searchDTO.setUsername(userDetails.getUsername());
        searchDTO.setRole(userDetails.getUser().getRole());

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(storeService.getStores(searchDTO, pageable))));
    }

    // get 상세 조회
    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreResponseDTO>> getStoreById(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(storeService.getStoreById(storeId, userDetails.getUser())));
    }

    // update (OWNER, MASTER 권한)
    @PutMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MASTER')")
    public ResponseEntity<ApiResponse<StoreResponseDTO>> updateStore(
            @PathVariable UUID storeId,
            @Valid @RequestBody StoreRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(storeService.updateStore(storeId, requestDTO, userDetails.getUser())));
    }

    // hide 가게 숨김 여부 수정 (PATCH)
    @PatchMapping("/{storeId}/hide")
    @PreAuthorize("hasAnyRole('OWNER', 'MASTER')")
    public ResponseEntity<ApiResponse<StoreResponseDTO>> updateStoreVisibility(
            @PathVariable UUID storeId,
            @Valid @RequestBody StoreVisibilityRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(storeService.updateVisibility(storeId, requestDTO.getIsHidden(), userDetails.getUser())));
    }

    // delete
    @DeleteMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MASTER')")
    public ResponseEntity<ApiResponse<StoreResponseDTO>> deleteStore(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(storeService.deleteStore(storeId, userDetails.getUser())));
    }
}
