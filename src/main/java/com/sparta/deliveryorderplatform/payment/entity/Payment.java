package com.sparta.deliveryorderplatform.payment.entity;

import com.sparta.deliveryorderplatform.global.entity.BaseAuditEntity;
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
public class Payment extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod = PaymentMethod.CARD;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private String username;

    public static Payment create(UUID orderId, Integer amount, String username) {
        return Payment.builder()
                .orderId(orderId)
                .amount(amount)
                .username(username)
                .paymentStatus(PaymentStatus.COMPLETED)
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
