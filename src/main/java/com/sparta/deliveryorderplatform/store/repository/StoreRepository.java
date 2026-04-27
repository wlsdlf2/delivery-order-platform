package com.sparta.deliveryorderplatform.store.repository;

import com.sparta.deliveryorderplatform.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID>, StoreRepositoryCustom {
    Optional<Store> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByAreaIdAndDeletedAtIsNull(UUID areaId);
}
