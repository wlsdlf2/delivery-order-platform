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
    Page<Order> findAllByStore_Id(UUID storeId, Pageable pageable);
    Page<Order> findAllByStatusAndUser_username(String status, String username, Pageable pageable);
    Page<Order> findAllByUser_username(String username, Pageable pageable);
}
