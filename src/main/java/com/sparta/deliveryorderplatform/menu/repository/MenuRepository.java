package com.sparta.deliveryorderplatform.menu.repository;

import com.sparta.deliveryorderplatform.menu.entity.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID> {
    Optional<Menu> findByMenuIdAndDeletedAtIsNullAndIsHiddenFalse(UUID menuId);

    //keyword 검색 + CUSTOMER
    Page<Menu> findByStore_idAndNameContainingAndDeletedAtIsNullAndIsHiddenFalse(UUID storeId, String name, Pageable pageable);

    //keyword 검색 + OWNER, MASTER
    Page<Menu> findByStore_idAndNameContainingAndDeletedAtIsNull(UUID storeId, String name, Pageable pageRequest);

    //전체 조회 + CUSTOMER
    Page<Menu> findByStore_idAndDeletedAtIsNullAndIsHiddenFalse(UUID storeId, Pageable pageRequest);

    //전체 조회 + OWNER, MASTER
    Page<Menu> findByStore_idAndDeletedAtIsNull(UUID storeId, Pageable pageRequest);

    Optional<Menu> findByMenuIdAndDeletedAtIsNull(UUID menuId);

}
