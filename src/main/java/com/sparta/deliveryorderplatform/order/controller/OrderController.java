package com.sparta.deliveryorderplatform.order.controller;

import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import com.sparta.deliveryorderplatform.global.common.PageResponse;
import com.sparta.deliveryorderplatform.order.dto.OrderRequestDto;
import com.sparta.deliveryorderplatform.order.dto.OrderResponseDto;
import com.sparta.deliveryorderplatform.order.dto.OrderSearch;
import com.sparta.deliveryorderplatform.order.entity.OrderStatus;
import com.sparta.deliveryorderplatform.order.repository.OrderRepository;
import com.sparta.deliveryorderplatform.order.service.OrderItemService;
import com.sparta.deliveryorderplatform.order.service.OrderService;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderItemService orderItemService;


    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderResponseDto>>> getOrders(OrderSearch search,
        @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable,
        Authentication authentication) {
        PageResponse response = orderService.getAllOrders(search, pageable,authentication);
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    /**
     * 주문 취소 요청 - CUSTOEMR, MASTER 주문 생성 후 5분이내에 할 것.
     *
     * @param orderId
     * @param authentication
     * @return
     */
    @PatchMapping("/{orderId}/cancle")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable UUID orderId,
        Authentication authentication) {
        orderService.cancleOrder(orderId, authentication);
        return ResponseEntity.ok(ApiResponse.success());
    }


    /**
     * 주문 삭제 - MASTER만
     *
     * @param orderId        취소할 주문
     * @param authentication 사용자 인증 객체
     * @return 취소된 주문
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> deleteOrder(@PathVariable UUID orderId,
        Authentication authentication) {
        OrderResponseDto dto = orderService.deleteOrder(orderId, authentication);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }


    /**
     * 주문 상태 변경 : PENDING - > ACCEPTED
     *
     * @param orderId
     * @param auth
     * @return
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<Void>> updateOrderStatus(
        @PathVariable UUID orderId, @RequestBody String status, Authentication auth) {
        //주문 상태 변경 메서드 호출.
        orderService.updateOrderStatus(orderId, status, auth);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 주문 요청 사항 변경
     *
     * @param orderId         주문 식별자
     * @param orderRequestDto 변경된 주문요청 데이터
     * @param authentication  로그인한 사용자 정보
     * @return 주문 응답객체
     */
    @PutMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Void>> updateOrderRequest(
        @PathVariable UUID orderId, @RequestBody OrderRequestDto orderRequestDto,
        Authentication authentication) {
        // 주문사항 요청 메서드 호출.
        orderService.updateOrderRequest(orderId, orderRequestDto, authentication);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 주문 생성 및 주문 메뉴 추가.
     *
     * @param orderRequestDto : 주문 요청
     * @return : 생성된 주문을 응답
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDto>> createOrder(
        @RequestBody OrderRequestDto orderRequestDto, Authentication authentication) {

        // 주문 생성
        OrderResponseDto newOrder = orderService.createOrder(orderRequestDto, authentication);

        // 주문 메뉴 추가.
        orderItemService.createOrderitem(orderRequestDto, newOrder.getOrderId());

        return ResponseEntity.ok(ApiResponse.success(newOrder));
    }


}
