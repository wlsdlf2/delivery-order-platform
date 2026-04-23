package com.sparta.deliveryorderplatform.store.service;

import com.sparta.deliveryorderplatform.area.entity.Area;
import com.sparta.deliveryorderplatform.area.repository.AreaRepository;
import com.sparta.deliveryorderplatform.category.entity.Category;
import com.sparta.deliveryorderplatform.category.repository.CategoryRepository;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.store.dto.StoreRequestDTO;
import com.sparta.deliveryorderplatform.store.dto.StoreResponseDTO;
import com.sparta.deliveryorderplatform.store.entity.Store;
import com.sparta.deliveryorderplatform.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final AreaRepository areaRepository;

    @Transactional
    public StoreResponseDTO createStore(StoreRequestDTO requestDTO, String username) {
        Category category = categoryRepository.getReferenceById(requestDTO.getCategoryId());
        Area area = areaRepository.getReferenceById(requestDTO.getAreaId());

        Store store = Store.create(
                requestDTO.getName(),
                requestDTO.getAddress(),
                requestDTO.getPhone(),
                username, // ownerId를 현재 로그인 유저로 설정
                category,
                area
        );

        return StoreResponseDTO.from(storeRepository.save(store));
    }

    @Transactional(readOnly = true)
    public StoreResponseDTO getStoreById(UUID storeId) {
        Store store = findStoreById(storeId);
        return StoreResponseDTO.from(store);
    }

    @Transactional(readOnly = true)
    public Page<StoreResponseDTO> getStores(Pageable pageable) {
        // TODO: QueryDSL 도입 시 검색 필터링 추가 예정
        return storeRepository.findAll(pageable).map(StoreResponseDTO::from);
    }


    // todo (기본) owner가 자기 가게만. 메뉴 정보를 업데이할 필요가...?
    // 권한 별로 수정 조건 추가. 관리자일 경우 모든 가게 수정 가능하도록.
    @Transactional
    public StoreResponseDTO updateStore(UUID storeId, StoreRequestDTO requestDTO) {
        Store store = findStoreById(storeId);
        
        Category category = categoryRepository.getReferenceById(requestDTO.getCategoryId());
        Area area = areaRepository.getReferenceById(requestDTO.getAreaId());

        store.update(
                requestDTO.getName(),
                requestDTO.getAddress(),
                requestDTO.getPhone(),
                category,
                area,
                requestDTO.getIsHidden()
        );

        return StoreResponseDTO.from(store);
    }

    // todo (기본) owner가 자기 가게만 & 완료되지 않은 주문 건이 있을 경우 삭제 안되게..?
    // 권한 별로 삭제 조건 추가. 관리자일 경우 모든 가게 삭제 가능하도록.
    @Transactional
    public StoreResponseDTO deleteStore(UUID storeId, String username) {
        Store store = findStoreById(storeId);
        store.delete(username);
        return StoreResponseDTO.from(store);
    }

    private Store findStoreById(UUID storeId) {
        return storeRepository.findByIdAndDeletedAtIsNull(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
    }
}
