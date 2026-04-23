package com.sparta.deliveryorderplatform.payment.dto.request;

import com.sparta.deliveryorderplatform.payment.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdatePaymentStatusRequest {

    @NotNull
    private PaymentStatus paymentStatus;
}
