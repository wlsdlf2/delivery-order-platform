package com.sparta.deliveryorderplatform.payment.controller;

import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import com.sparta.deliveryorderplatform.payment.dto.request.UpdatePaymentStatusRequest;
import com.sparta.deliveryorderplatform.payment.dto.request.CreatePaymentRequest;
import com.sparta.deliveryorderplatform.payment.dto.response.PaymentResponse;
import com.sparta.deliveryorderplatform.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
     * todo : 결제 상태에 관해 고민해봐야 할듯
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

    /**
     * 결제 상태 수정 api
     * todo : 권한 처리
     * @param paymentId
     * @param request
     * @return
     */
    @PatchMapping("/payments/{paymentId}")
    public ApiResponse<?> updatePaymentStatus(@PathVariable UUID paymentId,
                                              @Valid @RequestBody UpdatePaymentStatusRequest request) {

        PaymentResponse response = paymentService.updatePaymentStatus(paymentId, request);
        return ApiResponse.success(response);
    }

    /**
     * 결제 삭제 api(soft delete)
     * todo : 권한 처리
     * @param paymentId
     * @return
     */
    @PatchMapping("/payments/{paymentId}")
    public ResponseEntity<?> deletePayment(@PathVariable UUID paymentId) {

        paymentService.deletePayment(paymentId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
