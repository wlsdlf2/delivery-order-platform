package com.sparta.deliveryorderplatform.payment.entity;

import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.payment.dto.request.UpdatePaymentStatusRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_payment")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.CARD;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    private Integer amount;

    // audit field

    public static Payment create(UUID orderId, Integer amount) {
        return Payment.builder()
                .orderId(orderId)
                .amount(amount)
                .build();
    }

    public void updatePaymentStatus(UpdatePaymentStatusRequest request) {
        if (this.paymentStatus == PaymentStatus.CANCELLED) {
            throw new CustomException(ErrorCode.ALREADY_CANCELLED);
        }
        if (this.paymentStatus == PaymentStatus.COMPLETED
                && request.getPaymentStatus() == PaymentStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        this.paymentStatus = request.getPaymentStatus();
    }
}
