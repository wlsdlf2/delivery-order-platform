package com.sparta.deliveryorderplatform.area.service;

import com.sparta.deliveryorderplatform.area.dto.AreaRequestDTO;
import com.sparta.deliveryorderplatform.area.dto.AreaResponseDTO;
import com.sparta.deliveryorderplatform.area.dto.AreaSearchDTO;
import com.sparta.deliveryorderplatform.area.entity.Area;
import com.sparta.deliveryorderplatform.area.repository.AreaRepository;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        validateDuplicateName(requestDTO.getName());

        Area area = Area.create(requestDTO.getName(), requestDTO.getCity(), requestDTO.getDistrict());
        Area savedArea = areaRepository.save(area);
        return AreaResponseDTO.from(savedArea);
    }

    // get 목록 조회
    @Transactional(readOnly = true)
    public Page<AreaResponseDTO> getAreas(AreaSearchDTO searchDTO, UserRole role, Pageable pageable) {
        // 관리자 여부 판단
        boolean isAdmin = role == UserRole.MASTER;
        if (!isAdmin) searchDTO.setIsActive(true);  // 관리자가 아니라면 항상 활성 지역만 보도록 강제함

        return areaRepository.searchAreas(searchDTO, pageable).map(AreaResponseDTO::from);
    }

    // get 상세 조회
    @Transactional(readOnly = true)
    public AreaResponseDTO getAreaById(UUID areaId) {
        Area area = findAreaById(areaId);
        return AreaResponseDTO.from(area);
    }

    // update
    @Transactional
    public AreaResponseDTO updateArea(UUID areaId, AreaRequestDTO requestDTO) {
        Area area = findAreaById(areaId);

        // 지역명이 변경될 때만 운영지역명 중복 체크
        if (!area.getName().equals(requestDTO.getName())) {
            validateDuplicateName(requestDTO.getName());
        }

        area.update(requestDTO.getName(), requestDTO.getCity(), requestDTO.getDistrict(), requestDTO.getIsActive());
        return AreaResponseDTO.from(area);
    }

    // delete
    // todo store 기능 구현 시, 연관관계가 있는 경우 삭제되지 않도록 수정 필요
    @Transactional
    public AreaResponseDTO deleteArea(UUID areaId, String username) {
        Area area = findAreaById(areaId);
        area.delete(username);
        return AreaResponseDTO.from(area);
    }

    // 헬퍼 메서드: 유효한(삭제되지 않고 활성화된) 운영 지역 조회
    @Transactional(readOnly = true)
    public Area findActiveAreaById(UUID areaId) {
        return areaRepository.findByIdAndIsActiveTrueAndDeletedAtIsNull(areaId)
                .orElseThrow(() -> new CustomException(ErrorCode.AREA_NOT_FOUND));
    }

    // 헬퍼 메서드: 삭제되지 않은 데이터 조회
    private Area findAreaById(UUID areaId) {
        return areaRepository.findByIdAndDeletedAtIsNull(areaId)
            .orElseThrow(() -> new CustomException(ErrorCode.AREA_NOT_FOUND));
    }

    // 헬퍼 메서드: 지역명 중복 조회
    private void validateDuplicateName(String name) {
        if (areaRepository.existsByNameAndDeletedAtIsNull(name)) {
            throw new CustomException(ErrorCode.DUPLICATE_AREA_NAME);
        }
    }
}
