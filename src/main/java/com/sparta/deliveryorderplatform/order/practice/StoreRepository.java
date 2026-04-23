package com.sparta.deliveryorderplatform.order.practice;

import com.sparta.deliveryorderplatform.store.entity.Store;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, UUID> {
}
