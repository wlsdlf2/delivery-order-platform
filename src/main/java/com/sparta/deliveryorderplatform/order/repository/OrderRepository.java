package com.sparta.deliveryorderplatform.order.repository;

import com.sparta.deliveryorderplatform.order.entity.Order;
import com.sparta.deliveryorderplatform.order.entity.OrderStatus;
import com.sparta.deliveryorderplatform.store.entity.Store;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    Page<Order> findAllByStore_Id(UUID storeId, Pageable pageable);

    Page<Order> findAllByStore_IdAndStatus(UUID storeId, OrderStatus status, Pageable pageable);

//    Page<Order> findAllByStatusAndUser_username(String status, String username, Pageable pageable);

    Page<Order> findAllByUser_username(String username, Pageable pageable);

    Page<Order> findAllByUser_usernameAndStatus(String username, OrderStatus status, Pageable pageable);

    Page<Order>findAllByUser_usernameAndStore_id(String username, UUID storeId, Pageable pageable);

    Page<Order>findAllByUser_usernameAndStatusAndStore_id(String username,  OrderStatus status, UUID storeId, Pageable pageable);

    boolean existsByStore_IdAndStatusNotIn(UUID storeId, Collection<OrderStatus> statuses);

    Page<Order> findAllByStatus(OrderStatus status, Pageable page);

    Page<Order> findAllByStore_id(UUID storeId, Pageable page);

    Page<Order> findAllByStatusAndStore_id(OrderStatus status, UUID storeId,Pageable page);

    UUID store(Store store);
}
