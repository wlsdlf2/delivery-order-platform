package com.sparta.deliveryorderplatform.payment.controller;

import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import com.sparta.deliveryorderplatform.global.common.PageResponse;
import com.sparta.deliveryorderplatform.payment.dto.request.CreatePaymentRequest;
import com.sparta.deliveryorderplatform.payment.dto.request.UpdatePaymentStatusRequest;
import com.sparta.deliveryorderplatform.payment.dto.response.PaymentResponse;
import com.sparta.deliveryorderplatform.payment.service.PaymentService;
import com.sparta.deliveryorderplatform.user.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
     * @param orderId
     * @param request
     * @param userDetails
     * @return PaymentResponse
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/orders/{orderId}/payments")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(@PathVariable UUID orderId,
                                           @Valid @RequestBody CreatePaymentRequest request,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {

        PaymentResponse response = paymentService.createPayment(orderId, request, userDetails.getUser());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 결제 목록 조회 api
     * @param page
     * @param size
     * @param userDetails
     * @return Page<PaymentResponse>
     */
    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getPaymentList(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Page<PaymentResponse> response = paymentService.getPaymentList(
                page,
                size,
                userDetails.getUser(),
                userDetails.getAuthorities().iterator().next().getAuthority()
        );

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(response)));
    }

    /**
     * 결제 상세 조회 api
     * @param paymentId
     * @param userDetails
     * @return PaymentResponse
     */
    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable UUID paymentId,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        PaymentResponse response = paymentService.getPaymentById(
                paymentId,
                userDetails.getUser(),
                userDetails.getAuthorities().iterator().next().getAuthority()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 결제 상태 수정 api
     * @param paymentId
     * @param request
     * @return PaymentResponse
     */
    @PreAuthorize("hasRole('MASTER')")
    @PatchMapping("/payments/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> updatePaymentStatus(@PathVariable UUID paymentId,
                                                 @Valid @RequestBody UpdatePaymentStatusRequest request) {

        PaymentResponse response = paymentService.updatePaymentStatus(paymentId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 결제 삭제 api(soft delete)
     * @param paymentId
     * @param userDetails
     * @return
     */
    @PreAuthorize("hasRole('MASTER')")
    @DeleteMapping("/payments/{paymentId}")
    public ResponseEntity<ApiResponse<Void>> deletePayment(@PathVariable UUID paymentId,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {

        paymentService.deletePayment(paymentId, userDetails.getUser());

        return ResponseEntity.ok(ApiResponse.success());
    }
}
