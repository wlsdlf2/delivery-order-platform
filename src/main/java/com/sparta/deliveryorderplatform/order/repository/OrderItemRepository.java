package com.sparta.deliveryorderplatform.order.repository;

import com.sparta.deliveryorderplatform.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
