package com.sparta.deliveryorderplatform.payment.repository;

import com.sparta.deliveryorderplatform.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomPaymentRepository {

    Page<Payment> findPaymentList(String username, String role, Pageable pageable);
}
