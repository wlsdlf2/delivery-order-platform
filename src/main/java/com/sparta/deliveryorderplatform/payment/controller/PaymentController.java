package com.sparta.deliveryorderplatform.payment.controller;

import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import com.sparta.deliveryorderplatform.payment.dto.request.UpdatePaymentStatusRequest;
import com.sparta.deliveryorderplatform.payment.dto.request.CreatePaymentRequest;
import com.sparta.deliveryorderplatform.payment.dto.response.PaymentResponse;
import com.sparta.deliveryorderplatform.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<?> createPayment(@PathVariable UUID orderId,
                                           @Valid @RequestBody CreatePaymentRequest request,
                                           @AuthenticationPrincipal String username) {

        PaymentResponse response = paymentService.createPayment(orderId, request, username);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 결제 목록 조회 api
     * @return
     */
    @GetMapping("/payments")
    public ResponseEntity<?> getPaymentList(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            Authentication authentication) {

        String username = (String) authentication.getPrincipal();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        Page<PaymentResponse> response = paymentService.getPaymentList(page, size, username, role);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 결제 상세 조회 api
     * todo : 권한 처리 해야 함, Payment에도 userId 추가하는 게 나아보임
     * @return
     */
    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<?> getPaymentById(@PathVariable UUID paymentId,
                                            Authentication authentication) {

        String username = (String) authentication.getPrincipal();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        PaymentResponse response = paymentService.getPaymentById(paymentId, username, role);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 결제 상태 수정 api
     * todo : 권한 처리
     * @param paymentId
     * @param request
     * @return
     */
    @PatchMapping("/payments/{paymentId}")
    public ResponseEntity<?> updatePaymentStatus(@PathVariable UUID paymentId,
                                              @Valid @RequestBody UpdatePaymentStatusRequest request) {

        PaymentResponse response = paymentService.updatePaymentStatus(paymentId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 결제 삭제 api(soft delete)
     * todo : 권한 처리
     * @param paymentId
     * @return
     */
    @DeleteMapping("/payments/{paymentId}")
    public ResponseEntity<?> deletePayment(@PathVariable UUID paymentId) {

        paymentService.deletePayment(paymentId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
