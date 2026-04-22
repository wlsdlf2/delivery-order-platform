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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/areas")
@RequiredArgsConstructor
public class AreaController {
    private final AreaService areaService;

    // create
    @PostMapping
    public ApiResponse<AreaResponseDTO> createArea(
        @Valid @RequestBody AreaRequestDTO requestDTO,
        @RequestHeader("X-Username") String username,
        @RequestHeader("X-Role") String role
    ) {
        return ApiResponse.success(areaService.createArea(requestDTO));
    }

    // get 목록 조회
    @GetMapping
    public ApiResponse<PageResponse<AreaResponseDTO>> getAreas(
        AreaSearchDTO searchDTO,
        @RequestHeader("X-Role") String role,
        Pageable pageable
    ) {
        return ApiResponse.success(PageResponse.of(areaService.getAreas(searchDTO, role, pageable)));
    }

    // get 상세 조회
    @GetMapping("/{areaId}")
    public ApiResponse<AreaResponseDTO> getAreaById(@PathVariable UUID areaId) {
        return ApiResponse.success(areaService.getAreaById(areaId));
    }

    // update
    @PutMapping("/{areaId}")
    public ApiResponse<AreaResponseDTO> updateArea(
        @PathVariable UUID areaId,
        @Valid @RequestBody AreaRequestDTO requestDTO,
        @RequestHeader("X-Username") String username,
        @RequestHeader("X-Role") String role
    ) {
        return ApiResponse.success(areaService.updateArea(areaId, requestDTO));
    }

    // delete
    @PatchMapping("/{areaId}")
    public ApiResponse<AreaResponseDTO> deleteArea(
        @PathVariable UUID areaId,
        @RequestHeader("X-Username") String username,
        @RequestHeader("X-Role") String role
    ) {
        return ApiResponse.success(areaService.deleteArea(areaId, username));
    }
}
