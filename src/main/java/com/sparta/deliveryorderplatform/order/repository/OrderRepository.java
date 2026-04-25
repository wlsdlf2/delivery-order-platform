package com.sparta.deliveryorderplatform.order.repository;

import com.sparta.deliveryorderplatform.order.entity.Order;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    Page<Order> findAllByStoreId(UUID storeId, Pageable pageable);
    Page<Order> findAllByStatusandUserusername(String status, String username, Pageable pageable);
    Page<Order> findAllByUserusername(String username, Pageable pageable);
}
