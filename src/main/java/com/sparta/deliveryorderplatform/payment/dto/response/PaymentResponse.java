package com.sparta.deliveryorderplatform.payment.dto.response;

import com.sparta.deliveryorderplatform.payment.entity.Payment;
import com.sparta.deliveryorderplatform.payment.entity.PaymentMethod;
import com.sparta.deliveryorderplatform.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class PaymentResponse {

    private UUID paymentId;

    private UUID orderId;

    private PaymentStatus paymentStatus;

    private PaymentMethod paymentMethod;

    private Integer amount;

    public static PaymentResponse from(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .paymentStatus(payment.getPaymentStatus())
                .paymentMethod(payment.getPaymentMethod())
                .amount(payment.getAmount())
                .build();
    }
}
