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

    /**
     * 결제 처리 api
     * todo : 권한 처리, 주문 정보 받아와서 금액 등 넣어야 함
     * @param orderId
     * @param request
     * @return
     */
    @PostMapping("/orders/{orderId}/payments")
    public ApiResponse<?> createPayment(@PathVariable UUID orderId, @Valid @RequestBody CreatePaymentRequest request) {

        PaymentResponse response = paymentService.createPayment(orderId, request);
        return ApiResponse.success(response);
    }

    /**
     * 결제 상세 조회 api
     * todo : 권한 처리 해야 함, Payment에도 userId 추가하는 게 나아보임
     * @return
     */
    @GetMapping("/payments/{paymentId}")
    public ApiResponse<?> getPaymentById(@PathVariable UUID paymentId) {

        PaymentResponse response = paymentService.getPaymentById(paymentId);
        return ApiResponse.success(response);
    }
}
