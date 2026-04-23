//package com.sparta.deliveryorderplatform.order.controller;
//
//import com.sparta.deliveryorderplatform.global.common.ApiResponse;
//import com.sparta.deliveryorderplatform.order.dto.OrderRequestDto;
//import com.sparta.deliveryorderplatform.order.dto.OrderResponseDto;
//import com.sparta.deliveryorderplatform.order.repository.OrderRepository;
//import com.sparta.deliveryorderplatform.order.service.OrderItemService;
//import com.sparta.deliveryorderplatform.order.service.OrderService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
////import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestHeader;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/v1/orders")
//public class OrderController {
//
//    @Autowired
//    private OrderService orderService;
//    @Autowired
//    private OrderItemService orderItemService;
//
//
//    /**
//     * 주문 생성 및 주문 메뉴 추가.
//     * @param orderRequestDto : 주문 요청
//     * @param username        : 사용자 식별자
//     * @return                : 생성된 주문을 응답
//     */
//    @PostMapping
//    public ResponseEntity<ApiResponse<OrderResponseDto>> createOrder(
//        @RequestBody OrderRequestDto orderRequestDto)
////        ,@AuthenticationPrincipal String username)
//    {
//        String username = "username";
//        OrderResponseDto newOrder =  orderService.createOrder(orderRequestDto, "username"); // 주문 생성
//        orderItemService.createOrderitem(orderRequestDto,username, newOrder.getOrderId());// 주문 메뉴 추가.
//
//        return ResponseEntity.ok(ApiResponse.success(newOrder)); // 생성된 Order를 반환.
//    }
//
//}
