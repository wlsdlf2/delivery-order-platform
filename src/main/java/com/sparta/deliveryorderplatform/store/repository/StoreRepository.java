package com.sparta.deliveryorderplatform.store.repository;

import com.sparta.deliveryorderplatform.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store,Long> {
}
