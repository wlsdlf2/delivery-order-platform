package com.sparta.deliveryorderplatform.store.service;

import com.sparta.deliveryorderplatform.area.entity.Area;
import com.sparta.deliveryorderplatform.area.service.AreaService;
import com.sparta.deliveryorderplatform.category.entity.Category;
import com.sparta.deliveryorderplatform.category.service.CategoryService;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.store.dto.StoreRequestDTO;
import com.sparta.deliveryorderplatform.store.dto.StoreResponseDTO;
import com.sparta.deliveryorderplatform.store.entity.Store;
import com.sparta.deliveryorderplatform.store.repository.StoreRepository;
import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.entity.UserRole;
import com.sparta.deliveryorderplatform.user.repository.UserRepository;
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
    private final CategoryService categoryService;
    private final AreaService areaService;
    private final UserRepository userRepository;

    @Transactional
    public StoreResponseDTO createStore(StoreRequestDTO requestDTO, String username) {
        // Category 삭제 여부 확인
        Category category = categoryService.findCategoryById(requestDTO.getCategoryId());
        // Area 활성화여부 확인
        Area area = areaService.findActiveAreaById(requestDTO.getAreaId());

        User owner = userRepository.findById(username)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED_ACCESS));

        Store store = Store.create(
                requestDTO.getName(),
                requestDTO.getAddress(),
                requestDTO.getPhone(),
                owner,
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
        return storeRepository.findAll(pageable).map(StoreResponseDTO::from);
    }

    @Transactional
    public StoreResponseDTO updateStore(UUID storeId, StoreRequestDTO requestDTO, String username) {
        Store store = findStoreById(storeId);
        User user = userRepository.findById(username)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED_ACCESS));

        validateStoreAccess(user, store);
        
        Category category = categoryService.findCategoryById(requestDTO.getCategoryId());
        Area area = areaService.findActiveAreaById(requestDTO.getAreaId());

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

    @Transactional
    public StoreResponseDTO updateVisibility(UUID storeId, Boolean isHidden, String username) {
        Store store = findStoreById(storeId);
        User user = userRepository.findById(username)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED_ACCESS));

        validateStoreAccess(user, store);
        
        store.updateVisibility(isHidden);
        return StoreResponseDTO.from(store);
    }

    @Transactional
    public StoreResponseDTO deleteStore(UUID storeId, String username) {
        Store store = findStoreById(storeId);
        User user = userRepository.findById(username)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED_ACCESS));

        validateStoreAccess(user, store);

        store.delete(username);
        return StoreResponseDTO.from(store);
    }

    private Store findStoreById(UUID storeId) {
        return storeRepository.findByIdAndDeletedAtIsNull(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
    }

    private void validateStoreAccess(User user, Store store) {
        // MASTER 권한은 모두 통과
        if (user.getRole() == UserRole.MASTER) {
            return;
        }
        // OWNER인 경우 본인의 가게인지 확인
        if (!store.getOwner().getUsername().equals(user.getUsername())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }
}
