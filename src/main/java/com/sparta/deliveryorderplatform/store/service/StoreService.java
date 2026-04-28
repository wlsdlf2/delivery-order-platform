package com.sparta.deliveryorderplatform.store.service;

import com.sparta.deliveryorderplatform.area.entity.Area;
import com.sparta.deliveryorderplatform.area.service.AreaService;
import com.sparta.deliveryorderplatform.category.entity.Category;
import com.sparta.deliveryorderplatform.category.service.CategoryService;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.menu.repository.MenuRepository;
import com.sparta.deliveryorderplatform.order.service.OrderService;
import com.sparta.deliveryorderplatform.store.dto.StoreRequestDTO;
import com.sparta.deliveryorderplatform.store.dto.StoreResponseDTO;
import com.sparta.deliveryorderplatform.store.dto.StoreSearchDTO;
import com.sparta.deliveryorderplatform.store.entity.Store;
import com.sparta.deliveryorderplatform.store.repository.StoreRepository;
import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final CategoryService categoryService;
    private final AreaService areaService;
    private final OrderService orderService;
    private final MenuRepository menuRepository;

    @Transactional
    public StoreResponseDTO createStore(StoreRequestDTO requestDTO, User user) {
        // Category 삭제 여부 확인
        Category category = categoryService.findCategoryById(requestDTO.getCategoryId());
        // Area 활성화여부 확인
        Area area = areaService.findActiveAreaById(requestDTO.getAreaId());

        Store store = Store.create(
                requestDTO.getName(),
                requestDTO.getAddress(),
                requestDTO.getPhone(),
                user,
                category,
                area
        );

        return StoreResponseDTO.from(storeRepository.save(store));
    }

    @Transactional(readOnly = true)
    public Page<StoreResponseDTO> getStores(StoreSearchDTO searchDTO, Pageable pageable) {
        return storeRepository.searchStores(searchDTO, pageable).map(StoreResponseDTO::from);
    }

    @Transactional(readOnly = true)
    public StoreResponseDTO getStoreById(UUID storeId, User user) {
        Store store = findStoreById(storeId);

        // 1. MASTER는 삭제되지 않은 모든 가게 조회 가능
        if (user.getRole() == UserRole.MASTER) {
            return StoreResponseDTO.from(store);
        }

        // 2. OWNER: 본인 가게일 경우 숨김 여부 상관없이 조회 가능
        if (user.getRole() == UserRole.OWNER) {
            if (store.getOwner().getUsername().equals(user.getUsername())) {
                return StoreResponseDTO.from(store);    // 본인 가게
            }
        }

        // 3. 그 외(CUSTOMER, 타 가게 OWNER): 숨김 처리된 가게는 조회 불가
        if (Boolean.TRUE.equals(store.getIsHidden())) {
            throw new CustomException(ErrorCode.STORE_NOT_FOUND);
        }

        return StoreResponseDTO.from(store);
    }

    @Transactional
    public StoreResponseDTO updateStore(UUID storeId, StoreRequestDTO requestDTO, User user) {
        Store store = findStoreById(storeId);

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
    public StoreResponseDTO updateVisibility(UUID storeId, Boolean isHidden, User user) {
        Store store = findStoreById(storeId);
        validateStoreAccess(user, store);

        if (Boolean.TRUE.equals(isHidden)) {
            // 숨김 -> 진행 중인 주문 여부 확인 -> 메뉴 전체 숨김
            validateNoActiveOrders(storeId);
            menuRepository.updateIsHiddenByStoreId(storeId, true);
        } else {
            // 노출 -> 메뉴 전체 노출
            menuRepository.updateIsHiddenByStoreId(storeId, false);
        }

        store.updateVisibility(isHidden);
        return StoreResponseDTO.from(store);
    }

    @Transactional
    public StoreResponseDTO deleteStore(UUID storeId, User user) {
        Store store = findStoreById(storeId);
        validateStoreAccess(user, store);

        validateNoActiveOrders(storeId);

        menuRepository.softDelete(storeId, LocalDateTime.now(), user.getUsername());

        // 가게 삭제
        store.delete(user.getUsername());
        return StoreResponseDTO.from(store);
    }

    private Store findStoreById(UUID storeId) {
        return storeRepository.findByIdAndDeletedAtIsNull(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
    }

    // 특정 가게에 대해 접근 권한이 있는지 확인
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

    // 진행중인 주문이 있는지 확인 ->
    private void validateNoActiveOrders(UUID storeId) {
        if (!orderService.storeInOrderIsCompleted(storeId)) {
            throw new CustomException(ErrorCode.EXIST_ACTIVE_ORDERS);
        }
    }
}
