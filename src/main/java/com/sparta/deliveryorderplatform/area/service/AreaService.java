package com.sparta.deliveryorderplatform.area.service;

import com.sparta.deliveryorderplatform.area.dto.AreaRequestDTO;
import com.sparta.deliveryorderplatform.area.dto.AreaResponseDTO;
import com.sparta.deliveryorderplatform.area.entity.Area;
import com.sparta.deliveryorderplatform.area.repository.AreaRepository;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AreaService {

    private final AreaRepository areaRepository;

    // create
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

        area.update(requestDTO.getName(), requestDTO.getCity(), requestDTO.getDistrict(), requestDTO.getIsActive());

        return AreaResponseDTO.from(area);
    }

    // delete
    @Transactional
    public AreaResponseDTO deleteArea(UUID areaId, String username) {
        Area area = findActiveArea(areaId);
        area.delete(username);
        return AreaResponseDTO.from(area);
    }

    private Area findActiveArea(UUID areaId) {
        return areaRepository.findByIdAndDeletedAtIsNull(areaId)
            .orElseThrow(() -> new CustomException(ErrorCode.AREA_NOT_FOUND));
    }
}
