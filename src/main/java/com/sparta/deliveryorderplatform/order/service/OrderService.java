package com.sparta.deliveryorderplatform.order.service;

import com.sparta.deliveryorderplatform.menu.entity.Menu;
import com.sparta.deliveryorderplatform.menu.repository.MenuRepository;
import com.sparta.deliveryorderplatform.order.dto.OrderItemRequestDto;
import com.sparta.deliveryorderplatform.order.dto.OrderRequestDto;
import com.sparta.deliveryorderplatform.order.dto.OrderResponseDto;
import com.sparta.deliveryorderplatform.order.entity.Order;
import com.sparta.deliveryorderplatform.order.entity.OrderStatus;
import com.sparta.deliveryorderplatform.order.entity.OrderType;
import com.sparta.deliveryorderplatform.order.practice.Address;
import com.sparta.deliveryorderplatform.order.practice.AddressRepository;
import com.sparta.deliveryorderplatform.order.practice.StoreRepository;
import com.sparta.deliveryorderplatform.order.repository.OrderRepository;
import com.sparta.deliveryorderplatform.store.entity.Store;
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

    @Autowired
    private OrderItemService orderItemService;

    /**
     *
     * @param orderId
     * @param auth
     */
    @Transactional
    public void updateOrderStatus(UUID orderId, String status, Authentication auth){
        //사용자의 username을 가져온다.
        String   username = (String) auth.getPrincipal();

        //수정된 order를 가져온다.
        Order updateOrder = orderRepository.findById(orderId).orElseThrow(()->new IllegalArgumentException("조회된 주문이 없음."));

        //이 사용자가 수정 요청한 사용자인지 검사한다.
        if(!username.equals(updateOrder.getUser().getUsername())) {
            throw new AccessDeniedException("사용자가 아님");
        }
        // 사용자의 권한 목록 중 가장 첫번째를 가져온다.
        String role = auth.getAuthorities().iterator().next().getAuthority();

        // 고객이면, 예외 던진다.
        if("ROLE_CUSTOMER".equals(role)) {
            throw new AccessDeniedException("권한없음");
        }
        // 가게 주인이라면
        if("ROLE_OWNER".equals(role)){
            //현재 OrderStatus를 다음 단계로 변경하고, 문자열로 변환하여 저장한다.
            status = updateOrder.getStatus().getNextStatus().name();
        }
        //그 외 마스터의 경우는 status를 사용하도록 한다.

        //Status만 변경하는 인스턴스 메서드를 호출하여 DB에 적용시킨다.
        updateOrder.statusUpdate(status);
    }

    /**
     * 주문 요청 사항 변경
     * @param orderId  주문 식별자
     * @param orderReq 주문 요청 객체
     * @param auth   로그인한 사용자 정보
     * @return  주문 응답객체
     */
    @Transactional
    public void updateOrderRequest(UUID orderId, OrderRequestDto orderReq, Authentication auth) {
        // 사용자 식별자 추출.
        String  username = (String) auth.getPrincipal();
        // 수정할 Order를 조회
        Order updateOrder = orderRepository.findById(orderId).orElseThrow(()-> new IllegalAccessError("수정할 주문이 없습니다."));

        //이 사용자가 수정 요청한 사용자인지 판단.
        if(!username.equals(updateOrder.getUser().getUsername())) {
            throw new AccessDeniedException("사용자가 아님");
        }

        // 사용자의 인증 객체에서 권한 목록을 추출
        boolean isAllowed = auth.getAuthorities().stream()
            //권한 목록 중에서, 조건 검사를 수행 : True/False
            .anyMatch(grantedAuthority -> {
                //어떤 권한인지 문자열로 추출.
                String role =  grantedAuthority.getAuthority();
                //권한이 마스터라면 그냥 true 반환.
                if (role.equals("ROLE_MASTER")) {
                    return true;
                }
                //권한이 고객이고, 주문 상태가 PENDING 이라면 true 반환
                if (role.equals("ROLE_CUSTOMER") && updateOrder.getStatus() == OrderStatus.PENDING) {
                    return true;
                }
                // 그 외는 false
                return false;
            });

        //그 외의 경우는 요청 수정 권한이 없으므로 예외 던짐.
        if(!isAllowed) {
            throw new AccessDeniedException("권한 없음");
        }
        //가게 조회
        Store store =  storeRepository.findById(orderReq.getStoreId()).orElseThrow(() ->new IllegalArgumentException("가게가 없음."));
        // 주소 조회
        Address address = addressRepository.findById(orderReq.getAddressId()).orElseThrow(() ->new IllegalArgumentException("주소가 없음."));
        // Order 데이터 업데이트 (전체 필드 반영)
        updateOrder.update(orderReq,store,address);

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
