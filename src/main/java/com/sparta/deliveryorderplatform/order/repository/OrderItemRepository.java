package com.sparta.deliveryorderplatform.order.repository;

import com.sparta.deliveryorderplatform.order.entity.OrderItem;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

}
