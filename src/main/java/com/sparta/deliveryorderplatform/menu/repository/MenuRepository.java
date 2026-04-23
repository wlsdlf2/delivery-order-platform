package com.sparta.deliveryorderplatform.menu.repository;

import com.sparta.deliveryorderplatform.menu.entity.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID> {
    Optional<Menu> findByMenuIdAndDeletedAtIsNullAndIsHiddenFalse(UUID menuId);

    Page<Menu> findByStore_StoreIdAndDeletedAtIsNullAndIsHiddenFalse(UUID storeId, Pageable pageable);

    Page<Menu> findByStore_StoreIdAndDeletedAtIsNull(UUID storeId, Pageable pageRequest);

    Optional<Menu> findByMenuIdAndDeletedAtIsNull(UUID menuId);

}
