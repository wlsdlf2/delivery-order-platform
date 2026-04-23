package com.sparta.deliveryorderplatform.order.service;

import com.sparta.deliveryorderplatform.menu.entity.Menu;
import com.sparta.deliveryorderplatform.menu.repository.MenuRepository;
import com.sparta.deliveryorderplatform.order.dto.OrderItemRequestDto;
import com.sparta.deliveryorderplatform.order.dto.OrderRequestDto;
import com.sparta.deliveryorderplatform.order.dto.OrderResponseDto;
import com.sparta.deliveryorderplatform.order.entity.Order;
import com.sparta.deliveryorderplatform.order.entity.OrderStatus;
import com.sparta.deliveryorderplatform.order.practice.Address;
import com.sparta.deliveryorderplatform.order.practice.AddressRepository;
import com.sparta.deliveryorderplatform.order.practice.Store;
import com.sparta.deliveryorderplatform.order.practice.StoreRepository;
import com.sparta.deliveryorderplatform.order.repository.OrderRepository;
import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    //현재는 오류가 발생하므로, 주석 처리
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private MenuRepository menuRepository;

    /**
     * 주문 요청 사항 변경
     * @param orderId  주문 식별자
     * @param orderReq 주문 요청 객체
     * @param auth   로그인한 사용자 정보
     * @return  주문 응답객체
     */
    public void updateOrderRequest(UUID orderId, OrderRequestDto orderReq, Authentication auth) {
        // 사용자 식별자 추출.
        String  username = (String) auth.getPrincipal();
        // 수정할 Order를 조회
        Order updateOrder = orderRepository.findById(orderId)
                                           .orElseThrow(()-> new IllegalAccessError("수정할 주문이 없습니다."));
        boolean isAllowed = auth.getAuthorities().stream()          //인증 객체에서 사용자의 권한 목록을 추출하고,
            .anyMatch(grantedAuthority -> {
                String role =  grantedAuthority.getAuthority();     //권한을 추출해서
                if (role.equals("ROLE_MASTER")) {                   //마스터이면 요청 수정 가능
                    return true;
                }
                                //고객이면서 주문 상태가 PENDING일 때에만 주문 요청 수정 가능
                if (role.equals("ROLE_CUSTOMER") && updateOrder.getStatus() == OrderStatus.PENDING) {
                    return true;
                }
                // 그 외의 경우 종료.
                return false;
            });

        if(!isAllowed) { // 해당 조건에 맞지 않으면, 예외를 던진다.
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }
        // 데이터 업데이트 (전체 필드 반영)
        updateOrder.update(orderReq);
    }


    /**
     * 주문 생성 - 주문 테이블만
     * @param orderRequestDto : 주문 요청
     * @param username        : 사용자 식별자
     * @return                : 생성된 주문을 응답.
     */
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto, String username) {
        //현재는 오류가 나므로 주석 처리
        //username으로 User 조회
        User user = userRepository.findById(username).orElseThrow(() ->
            new IllegalAccessError("사용자가 없습니다."));

        //storeId로 Store 조회
        Store store = storeRepository.findById(orderRequestDto.getStoreId()).orElseThrow(() ->
            new IllegalAccessError("가게가 없습니다."));

        //addressId로 Address 조회
        Address address = addressRepository.findById(orderRequestDto.getAddressId()).orElseThrow(() ->
            new IllegalAccessError("주소가 없습니다."));

        //totalPrice 계산
        Integer totalPrice = 0;
        List<OrderItemRequestDto> items =  orderRequestDto.getItems(); // 요청 dto에서 주문메뉴 목록을 추출.
        for (OrderItemRequestDto item : items) {                       // 다음 주문 메뉴 목록을 순회하여,
            Menu orderedMenu = menuRepository.findById(item.getMenuId()).orElseThrow(() ->
                new IllegalAccessError("주소가 없습니다."));          // Menu를 DB에서 찾고,
            totalPrice += orderedMenu.getPrice()*item.getQuantity();  // Menu의 price와 수량을 곱하여 총 금맥을 계산.
        }

        //새로운 Order 객체로 변환
        Order newOrder = Order.createOrder(user
                                          ,store
                                          ,address
                                          ,orderRequestDto.getOrderType()
                                          ,totalPrice
                                          ,orderRequestDto.getRequest());

        orderRepository.save(newOrder); // DB에 저장.
        return OrderResponseDto.from(newOrder);
    }




}
