package com.sparta.deliveryorderplatform.order.service;

import com.sparta.deliveryorderplatform.order.dto.OrderItemRequestDto;
import com.sparta.deliveryorderplatform.order.dto.OrderRequestDto;
import com.sparta.deliveryorderplatform.order.entity.Order;
import com.sparta.deliveryorderplatform.order.entity.OrderItem;
import com.sparta.deliveryorderplatform.order.repository.OrderItemRepository;
import com.sparta.deliveryorderplatform.order.repository.OrderRepository;
import java.awt.Menu;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderItemService {

//    @Autowired
//    private OrderRepository orderRepository;
//    @Autowired
//    private OrderItemRepository orderItemRepository;
//
//    @Autowired
//    private MenuRepository menuRepository;
//
//    /**
//     * 주문 생성 시, 주문 메뉴 추가.
//     * @param orderRequestDto : 주문 메뉴 객체
//     * @param username        : 사용자 식별자.
//     */
//    @Transactional
//    public void createOrderitem(OrderRequestDto orderRequestDto, String username, UUID orderId) {
//        List<OrderItemRequestDto> items = orderRequestDto.getItems(); // 주문 요청 객체에서 주문 메뉴 리스트를 추출.
//
//        List<OrderItem> orderItems = new ArrayList<>();               // DB에 저장 시킬 주문 메뉴 리스트 선언.
//        Order ordered = orderRepository.findById(orderId)
//                                       .orElseThrow(()-> new IllegalArgumentException("주문이 없습니다.")); //새롭게 생성한 Order를 가져온다.
//        //주문 메뉴 만들기.
//        for (OrderItemRequestDto item : items) { // 주문메뉴 리스트를 반복하여,
//            Menu orderedMenu = menuRepository.findById(item.getMenuId())
//                                             .orElseThrow(() -> new IllegalAccessError("주소가 없습니다.")); // 주문 메뉴 안의 menuId로 Menu를 가져온다.
//            orderItems.add(OrderItem.createOrderItem(ordered, orderedMenu, item.getQuantity(), orderedMenu.getPrice())); //
//        }
//
//        orderItemRepository.saveAll(orderItems); // 이 리스트를 모두 DB에 저장.
//    }

}
