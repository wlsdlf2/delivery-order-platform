package com.sparta.deliveryorderplatform.payment.repository;

import com.sparta.deliveryorderplatform.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomPaymentRepository {

    Page<Payment> findPaymentList(String username, String role, Pageable pageable);

    String findOwnerUsernameByPaymentId(UUID paymentId);
}
