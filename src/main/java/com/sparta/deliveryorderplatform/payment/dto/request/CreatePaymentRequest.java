package com.sparta.deliveryorderplatform.payment.dto.request;

import com.sparta.deliveryorderplatform.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreatePaymentRequest {

    @NotNull
    private PaymentMethod paymentMethod;
}
