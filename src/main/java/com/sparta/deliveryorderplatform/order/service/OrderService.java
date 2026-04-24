//package com.sparta.deliveryorderplatform.order.service;
//
//import com.sparta.deliveryorderplatform.order.dto.OrderItemRequestDto;
//import com.sparta.deliveryorderplatform.order.dto.OrderRequestDto;
//import com.sparta.deliveryorderplatform.order.dto.OrderResponseDto;
//import com.sparta.deliveryorderplatform.order.entity.Order;
//import com.sparta.deliveryorderplatform.order.repository.OrderRepository;
//import java.awt.Menu;
//import java.util.List;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//@Transactional(readOnly = true)
//public class OrderService {
//
//    @Autowired
//    private OrderRepository orderRepository;
//
//    //현재는 오류가 발생하므로, 주석 처리
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private StoreRepository storeRepository;
//
//    @Autowired
//    private AddressRepository addressRepository;
//
//    @Autowired
//    private MenuRepository menuRepository;
//
//    /**
//     * 주문 생성 - 주문 테이블만
//     * @param orderRequestDto : 주문 요청
//     * @param username        : 사용자 식별자
//     * @return                : 생성된 주문을 응답.
//     */
//    @Transactional
//    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto, String username) {
//        //현재는 오류가 나므로 주석 처리
//        //username으로 User 조회
//        User user = userRepository.findById(username).orElseThrow(() ->
//            new IllegalAccessError("사용자가 없습니다."));
//
//        //storeId로 Store 조회
//        Store store = storeRepository.findById(orderRequestDto.getStoreId()).orElseThrow(() ->
//            new IllegalAccessError("가게가 없습니다."));
//
//        //addressId로 Address 조회
//        Address address = addressRepository.findById(orderRequestDto.getAddressId()).orElseThrow(() ->
//            new IllegalAccessError("주소가 없습니다."));
//
//        //totalPrice 계산
//        Integer totalPrice = 0;
//        List<OrderItemRequestDto> items =  orderRequestDto.getItems(); // 요청 dto에서 주문메뉴 목록을 추출.
//        for (OrderItemRequestDto item : items) {                       // 다음 주문 메뉴 목록을 순회하여,
//            Menu orderedMenu = menuRepository.findById(item.getMenuId()).orElseThrow(() ->
//                new IllegalAccessError("주소가 없습니다."));          // Menu를 DB에서 찾고,
//            totalPrice += orderedMenu.getPrice()*item.getQuantity();  // Menu의 price와 수량을 곱하여 총 금맥을 계산.
//        }
//
//        //새로운 Order 객체로 변환
//        Order newOrder = Order.createOrder(user
//                                          ,store
//                                          ,address
//                                          ,orderRequestDto.getOrderType()
//                                          ,totalPrice
//                                          ,orderRequestDto.getRequest());
//
//        orderRepository.save(newOrder); // DB에 저장.
//        return OrderResponseDto.from(newOrder);
//    }
//
//
//
//
//}
