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
    public AreaResponseDTO createArea(AreaRequestDTO requestDTO) {
        // 운영지역명 중복 체크
        validateDuplicateName(requestDTO.getName());

        Area area = Area.create(requestDTO.getName(), requestDTO.getCity(), requestDTO.getDistrict());
        Area savedArea = areaRepository.save(area);
        return AreaResponseDTO.from(savedArea);
    }



    // get 목록 조회

    // get 상세 조회

    // update
    @Transactional
    public AreaResponseDTO updateArea(UUID areaId, AreaRequestDTO requestDTO) {
        // 삭제되지 않은 데이터 조회
        Area area = findActiveArea(areaId);

        // 지역명이 변경될 때만 운영지역명 중복 체크
        if (!area.getName().equals(requestDTO.getName())) {
            validateDuplicateName(requestDTO.getName());
        }

        area.update(requestDTO.getName(), requestDTO.getCity(), requestDTO.getDistrict(), requestDTO.getIsActive());
        return AreaResponseDTO.from(area);
    }

    // delete
    @Transactional
    public AreaResponseDTO deleteArea(UUID areaId, String username) {
        // 삭제되지 않은 데이터 조회
        Area area = findActiveArea(areaId);
        area.delete(username);
        return AreaResponseDTO.from(area);
    }

    private Area findActiveArea(UUID areaId) {
        return areaRepository.findByIdAndDeletedAtIsNull(areaId)
            .orElseThrow(() -> new CustomException(ErrorCode.AREA_NOT_FOUND));
    }

    private void validateDuplicateName(String name) {
        if (areaRepository.existsByNameAndDeletedAtIsNull(name)) {
            throw new CustomException(ErrorCode.DUPLICATE_AREA_NAME);
        }
    }
}
