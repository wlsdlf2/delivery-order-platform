package com.sparta.deliveryorderplatform.order.controller;

import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import com.sparta.deliveryorderplatform.global.common.PageResponse;
import com.sparta.deliveryorderplatform.order.dto.OrderRequestDto;
import com.sparta.deliveryorderplatform.order.dto.OrderResponseDto;
import com.sparta.deliveryorderplatform.order.dto.OrderSearch;
import com.sparta.deliveryorderplatform.order.service.OrderItemService;
import com.sparta.deliveryorderplatform.order.service.OrderService;
import com.sparta.deliveryorderplatform.user.security.UserDetailsImpl;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderItemService orderItemService;

    /**
     * 주문 상세 조회
     * @param orderId 조회할 주문 식별자
     * @param impl 인증 객체
     * @return 상세 주문 내역 응답
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrder(@PathVariable UUID orderId,
        @AuthenticationPrincipal UserDetailsImpl impl) {
        OrderResponseDto dto = orderService.getOrder(orderId, impl.getUsername(),impl.getUser().getRole());
        return  ResponseEntity.ok(ApiResponse.success(dto));

    }

    /**
     * 전체 목록 조회
     *
     * @param search  검색 조건 - 주문 상태, 가게
     * @param pageable  페이징 조건
     * @param impl 인증 객체
     * @return
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderResponseDto>>> getOrders(OrderSearch search,
        @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable,
        @AuthenticationPrincipal UserDetailsImpl impl) {
        PageResponse response = orderService.getAllOrders(search, pageable, impl.getUsername(),impl.getUser().getRole());
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    /**
     * 주문 취소 요청 - CUSTOEMR, MASTER 주문 생성 후 5분이내에 할 것.
     *
     * @param orderId
     * @param impl
     * @return
     */
    @PreAuthorize("hasRole('ROLE_MASTER') or hasRole('ROLE_CUSTOMER')")
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable UUID orderId,
        @AuthenticationPrincipal UserDetailsImpl impl) {
        orderService.cancleOrder(orderId, impl.getUsername(), impl.getUser().getRole());
        return ResponseEntity.ok(ApiResponse.success());
    }


    /**
     * 주문 삭제 - MASTER만
     *
     * @param orderId        취소할 주문
     * @param impl 사용자 인증 객체
     * @return 취소된 주문
     */
    @PreAuthorize("hasRole('ROLE_MASTER')")
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> deleteOrder(@PathVariable UUID orderId,
        @AuthenticationPrincipal UserDetailsImpl impl) {
        OrderResponseDto dto = orderService.deleteOrder(orderId, impl.getUsername(),impl.getUser().getRole());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }


    /**
     * 주문 상태 변경 : PENDING - > ACCEPTED
     *
     * @param orderId
     * @param impl
     * @return
     */
    @PreAuthorize("hasRole('ROLE_MASTER' or hasRole('ROLE_CUSTOMER'))") // 관리자 혹은 고객인 경우만 접근 가능.
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<Void>> updateOrderStatus(
        @PathVariable UUID orderId, @RequestBody String status, @AuthenticationPrincipal UserDetailsImpl impl) {
        //주문 상태 변경 메서드 호출.
        orderService.updateOrderStatus(orderId, status, impl.getUsername(), impl.getUser().getRole());
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 주문 요청 사항 변경
     *
     * @param orderId         주문 식별자
     * @param orderRequestDto 변경된 주문요청 데이터
     * @param impl :  로그인한 사용자 정보
     * @return 주문 응답객체
     */
    @PreAuthorize("hasRole('ROLE_MASTER') or hasRole('ROLE_CUSTOMER')")// 관리자 혹은 고객
    @PutMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Void>> updateOrderRequest(
        @PathVariable UUID orderId, @RequestBody @Valid OrderRequestDto orderRequestDto,
        @AuthenticationPrincipal UserDetailsImpl impl) {
        // 주문사항 요청 메서드 호출.
        orderService.updateOrderRequest(orderId, orderRequestDto, impl.getUsername(),impl.getUser().getRole());
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 주문 생성 및 주문 메뉴 추가.
     *
     * @param orderRequestDto : 주문 요청
     * @return : 생성된 주문을 응답
     */
    @PreAuthorize("hasRole('ROLE_CUSTOMER')") // 고객만 주문 생성 가능
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDto>> createOrder(
        @RequestBody OrderRequestDto orderRequestDto,@AuthenticationPrincipal UserDetailsImpl impl) {

        // 주문 생성
        OrderResponseDto newOrder = orderService.createOrder(orderRequestDto, impl.getUsername());

        // 주문 메뉴 추가.
        orderItemService.createOrderitem(orderRequestDto, newOrder.getOrderId());

        return ResponseEntity.ok(ApiResponse.success(newOrder));
    }


}
