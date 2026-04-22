package com.sparta.deliveryorderplatform.order.repository;

import com.sparta.deliveryorderplatform.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
