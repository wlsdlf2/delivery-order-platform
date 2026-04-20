package com.sparta.deliveryorderplatform.payment.controller;

import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import com.sparta.deliveryorderplatform.payment.dto.request.CreatePaymentRequest;
import com.sparta.deliveryorderplatform.payment.dto.response.PaymentResponse;
import com.sparta.deliveryorderplatform.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/orders/{orderId}/payments")
    public ApiResponse<?> createPayment(@PathVariable UUID orderId, @Valid @RequestBody CreatePaymentRequest request) {

        PaymentResponse response = paymentService.createPayment(orderId, request);
        return ApiResponse.success(response);
    }
}
