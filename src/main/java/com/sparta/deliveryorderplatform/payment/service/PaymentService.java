package com.sparta.deliveryorderplatform.payment.service;

import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.payment.dto.request.CreatePaymentRequest;
import com.sparta.deliveryorderplatform.payment.dto.request.UpdatePaymentStatusRequest;
import com.sparta.deliveryorderplatform.payment.dto.response.PaymentResponse;
import com.sparta.deliveryorderplatform.payment.entity.Payment;
import com.sparta.deliveryorderplatform.payment.entity.PaymentMethod;
import com.sparta.deliveryorderplatform.payment.repository.PaymentRepository;
import com.sparta.deliveryorderplatform.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponse createPayment(UUID orderId, CreatePaymentRequest request, String username) {

        if (request.getPaymentMethod() != PaymentMethod.CARD) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_METHOD);
        }

        // 실제 주문 금액이랑 결제 요청 금액 비교 로직 추가
        // 주문한 사람이랑 결제 요청한 사람이랑 비교 로직 추가

        Payment payment = Payment.create(orderId, request.getAmount(), username);

        paymentRepository.save(payment);

        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentList(int page, int size, String username, String role) {

        if (size != 10 && size != 30 && size != 50) {
            size = 10;
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return paymentRepository.findPaymentList(username, role, pageRequest).map(PaymentResponse::from);
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

    @Transactional
    public void deletePayment(UUID paymentId) {

        Payment payment = this.findPaymentById(paymentId);

        payment.softDelete("user");
    }

    private Payment findPaymentById(UUID paymentId) {
        return paymentRepository.findByIdAndDeletedAtIsNull(paymentId).orElseThrow(
                () -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND)
        );
    }
}
