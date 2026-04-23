package com.sparta.deliveryorderplatform.store.controller;

import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import com.sparta.deliveryorderplatform.store.entity.Store;
import com.sparta.deliveryorderplatform.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;

    // create
    @PostMapping("/api/v1/stores")
    @PreAuthorize("hasAnyAuthority('MASTER')")
    public ResponseEntity<ApiResponse<>> createStore(@RequestBody StoreRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponse.success(storeService.createStore(requestDTO)));
    }


    // get 목록조회

    // get 1건 상세조회

    // update

    // delete

    // patch = hidden
}
