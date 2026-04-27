package com.sparta.deliveryorderplatform.payment.service;

import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.order.entity.Order;
import com.sparta.deliveryorderplatform.order.repository.OrderRepository;
import com.sparta.deliveryorderplatform.payment.dto.request.CreatePaymentRequest;
import com.sparta.deliveryorderplatform.payment.dto.request.UpdatePaymentStatusRequest;
import com.sparta.deliveryorderplatform.payment.dto.response.PaymentResponse;
import com.sparta.deliveryorderplatform.payment.entity.Payment;
import com.sparta.deliveryorderplatform.payment.entity.PaymentMethod;
import com.sparta.deliveryorderplatform.payment.repository.PaymentRepository;
import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public PaymentResponse createPayment(UUID orderId, CreatePaymentRequest request, User user) {

        if (request.getPaymentMethod() != PaymentMethod.CARD) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_METHOD);
        }

        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new CustomException(ErrorCode.ORDER_NOT_FOUND)
        );

        // 주문한 사람이랑 결제 요청한 사람이랑 비교
        if (!order.getUser().getUsername().equals(user.getUsername())) {
            throw new CustomException(ErrorCode.PAYMENT_USER_MISMATCH);
        }

        // 결제 중복 체크
        if (paymentRepository.existsByOrder(order)) {
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_EXISTS);
        }

        // 실제 주문 금액이랑 결제 요청 금액 비교
        if (!request.getAmount().equals(order.getTotalPrice())) {
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        Payment payment = Payment.create(order, request.getAmount(), user);

        paymentRepository.save(payment);

        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentList(int page, int size, User user, String role) {

        if (size != 10 && size != 30 && size != 50) {
            size = 10;
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return paymentRepository.findPaymentList(user.getUsername(), role, pageRequest).map(PaymentResponse::from);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID paymentId, User user, String role) {

        Payment payment = findPaymentById(paymentId);

        String username = user.getUsername();

        // customer는 본인 것만 조회 가능
        if (role.equals(UserRole.CUSTOMER.getAuthority()) &&
                !payment.getUser().getUsername().equals(username)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // owner는 본인, 본인 가게 결제만 조회 가능
        // todo : store owner 확인 로직 추가 (Store 연관관계 완성 후)
        if (role.equals(UserRole.OWNER.getAuthority())) {
            if (!payment.getUser().getUsername().equals(username)) {
                throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
            }
        }

        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse updatePaymentStatus(UUID paymentId, UpdatePaymentStatusRequest request) {

        Payment payment = this.findPaymentById(paymentId);

        payment.updatePaymentStatus(request);

        return PaymentResponse.from(payment);
    }

    @Transactional
    public void deletePayment(UUID paymentId, User user) {

        Payment payment = this.findPaymentById(paymentId);

        payment.softDelete(user.getUsername());
    }

    private Payment findPaymentById(UUID paymentId) {
        return paymentRepository.findByIdAndDeletedAtIsNull(paymentId).orElseThrow(
                () -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND)
        );
    }
}
