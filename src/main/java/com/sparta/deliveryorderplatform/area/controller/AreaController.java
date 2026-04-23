package com.sparta.deliveryorderplatform.area.controller;

import com.sparta.deliveryorderplatform.area.dto.AreaRequestDTO;
import com.sparta.deliveryorderplatform.area.dto.AreaResponseDTO;
import com.sparta.deliveryorderplatform.area.dto.AreaSearchDTO;
import com.sparta.deliveryorderplatform.area.service.AreaService;
import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import com.sparta.deliveryorderplatform.global.common.PageResponse;
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
@RequestMapping("/api/v1/areas")
@RequiredArgsConstructor
public class AreaController {
    private final AreaService areaService;

    // create
    @PostMapping
    @PreAuthorize("hasAuthority('MASTER')")
    public ResponseEntity<ApiResponse<AreaResponseDTO>> createArea(@Valid @RequestBody AreaRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponse.success(areaService.createArea(requestDTO)));
    }

    // get 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AreaResponseDTO>>> getAreas(
        AreaSearchDTO searchDTO,
        Authentication authentication,
        Pageable pageable
    ) {
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(areaService.getAreas(searchDTO, role, pageable))));
    }

    // get 상세 조회
    @GetMapping("/{areaId}")
    public ResponseEntity<ApiResponse<AreaResponseDTO>> getAreaById(@PathVariable UUID areaId) {
        return ResponseEntity.ok(ApiResponse.success(areaService.getAreaById(areaId)));
    }

    // update
    @PutMapping("/{areaId}")
    @PreAuthorize("hasAuthority('MASTER')")
    public ResponseEntity<ApiResponse<AreaResponseDTO>> updateArea(
        @PathVariable UUID areaId,
        @Valid @RequestBody AreaRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(ApiResponse.success(areaService.updateArea(areaId, requestDTO)));
    }

    // delete
    @PatchMapping("/{areaId}")
    @PreAuthorize("hasAuthority('MASTER')")
    public ResponseEntity<ApiResponse<AreaResponseDTO>> deleteArea(
        @PathVariable UUID areaId, Authentication authentication
    ) {
        String username = authentication.getName(); // principal에 담긴 username 반환
        return ResponseEntity.ok(ApiResponse.success(areaService.deleteArea(areaId, username)));
    }
}
