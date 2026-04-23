package com.sparta.deliveryorderplatform.store.repository;

import com.sparta.deliveryorderplatform.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {
    Optional<Store> findByIdAndDeletedAtIsNull(UUID id);
}
