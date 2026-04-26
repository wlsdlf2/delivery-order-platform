package com.sparta.deliveryorderplatform.payment.repository;

import com.sparta.deliveryorderplatform.order.entity.Order;
import com.sparta.deliveryorderplatform.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID>, CustomPaymentRepository {

    Optional<Payment> findByIdAndDeletedAtIsNull(UUID id);

    Boolean existsByOrder(Order order);
}
