package com.sparta.deliveryorderplatform.payment.service;

import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.payment.dto.request.CreatePaymentRequest;
import com.sparta.deliveryorderplatform.payment.dto.request.UpdatePaymentStatusRequest;
import com.sparta.deliveryorderplatform.payment.dto.response.PaymentResponse;
import com.sparta.deliveryorderplatform.payment.entity.Payment;
import com.sparta.deliveryorderplatform.payment.entity.PaymentMethod;
import com.sparta.deliveryorderplatform.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponse createPayment(UUID orderId, CreatePaymentRequest request) {

        if (request.getPaymentMethod() != PaymentMethod.CARD) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_METHOD);
        }

        Payment payment = Payment.create(orderId, 0);   // 결제 금액 주문 테이블에서 가져와야 하나?

        paymentRepository.save(payment);

        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID paymentId) {

        return PaymentResponse.from(this.findPaymentById(paymentId));
    }

    @Transactional
    public PaymentResponse updatePaymentStatus(UUID paymentId, UpdatePaymentStatusRequest request) {

        Payment payment = this.findPaymentById(paymentId);

        payment.updatePaymentStatus(request);

        return PaymentResponse.from(payment);
    }

    private Payment findPaymentById(UUID paymentId) {
        return paymentRepository.findById(paymentId).orElseThrow(
                () -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND)
        );
    }

    @Transactional
    public void deletePayment(UUID paymentId) {

        Payment payment = this.findPaymentById(paymentId);

        payment.softDelete("user");
    }
}
