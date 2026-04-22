package com.sparta.deliveryorderplatform.area.service;

import com.sparta.deliveryorderplatform.area.dto.AreaRequestDTO;
import com.sparta.deliveryorderplatform.area.dto.AreaResponseDTO;
import com.sparta.deliveryorderplatform.area.entity.Area;
import com.sparta.deliveryorderplatform.area.repository.AreaRepository;
import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AreaService {

    private final AreaRepository areaRepository;

    // create todo 권한처리
    @Transactional
    public AreaResponseDTO createArea(AreaRequestDTO requestDTO, String username, String role) {
        // 운영지역명 중복 체크
        if (areaRepository.existsByName(requestDTO.getName())) {
            throw new CustomException(ErrorCode.DUPLICATE_AREA_NAME);
        }

        Area area = Area.create(requestDTO.getName(), requestDTO.getCity(), requestDTO.getDistrict());
        Area savedArea = areaRepository.save(area);
        return AreaResponseDTO.from(savedArea);
    }



    // get 목록 조회

    // get 상세 조회

    // update
    // todo? @PreAuthorize("hasRole('MASTER')")
    @Transactional
    public AreaResponseDTO updateArea(UUID areaId, AreaRequestDTO requestDTO) {
        Area area = findActiveArea(areaId);

        // 지역명이 변경될 때만 중복 체크
        if (!area.getName().equals(requestDTO.getName()) &&
            areaRepository.existsByName(requestDTO.getName())) {
            throw new CustomException(ErrorCode.DUPLICATE_AREA_NAME);
        }

        area.Update(requestDTO.getName(), requestDTO.getCity(), requestDTO.getDistrict());
    }

    // todo? @PreAuthorize("hasRole('MASTER')")
    @Transactional
    public AreaResponseDTO updateStatus(UUID areaId, AreaRequestDTO requestDTO) {
        Area area = findActiveArea(areaId);
        area.Update(requestDTO.isActive());
    }

    // delete
}
