package com.sparta.deliveryorderplatform.order.service;

import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.menu.entity.Menu;
import com.sparta.deliveryorderplatform.menu.repository.MenuRepository;
import com.sparta.deliveryorderplatform.order.dto.OrderItemRequestDto;
import com.sparta.deliveryorderplatform.order.dto.OrderRequestDto;
import com.sparta.deliveryorderplatform.order.dto.OrderResponseDto;
import com.sparta.deliveryorderplatform.order.entity.Order;
import com.sparta.deliveryorderplatform.order.entity.OrderStatus;
import com.sparta.deliveryorderplatform.order.entity.OrderType;
import com.sparta.deliveryorderplatform.order.prac.Address;
import com.sparta.deliveryorderplatform.order.prac.AddressRepository;
import com.sparta.deliveryorderplatform.order.prac.StoreRepository;
import com.sparta.deliveryorderplatform.order.repository.OrderRepository;
import com.sparta.deliveryorderplatform.store.entity.Store;
import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.repository.UserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * 주문 삭제 - MASTER만
     * @param orderId 취소할 주문
     * @param auth 사용자 인증 객체
     * @return 취소된 주문
     */
    @Transactional
    public OrderResponseDto deleteOrder(UUID orderId,  Authentication auth) {
        // 사용자의 권한을 가져온다.
        String role = auth.getAuthorities().iterator().next().getAuthority();

        //권한이 마스터가 아닌 경우 예외를 던진다.
        if(!"ROLE_MASTER".equals(role)){
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        //삭제할 Order를 가져온다.
        Order deleteOrder = orderRepository.findById(orderId).orElseThrow(()-> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        //Order 엔티티의 삭제 인스턴스 메서드를 호출하여 삭제한다.
        deleteOrder.softDelete(auth.getName());
        return OrderResponseDto.from(deleteOrder);
    }

    /**
     * 주문 상태 변경
     * - OWNER와 MASTER가 할 수 있음.
     * @param orderId 변경할 현재 Order Id
     * @param status  변경할 status 값
     * @param auth    사용자 인증 객체
     */
    @Transactional
    public void updateOrderStatus(UUID orderId, String status, Authentication auth){
        //가게 주인이 다른 가게 주문에 접근한 것인지 확인하기 위해 인증 객체에서 사용자의 username을 가져온다.
        String   username = auth.getName();

        //변경할 현재 Order를 가져온다.
        Order updateOrder = orderRepository.findById(orderId).orElseThrow(()->new CustomException(ErrorCode.ORDER_NOT_FOUND));

        // 사용자의 권한 목록 중 가장 첫번째를 가져온다.
        String role = auth.getAuthorities().iterator().next().getAuthority();

        // 고객은 status 변경 권한이 없음.
        if("ROLE_CUSTOMER".equals(role)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 권한이 가게 주인이지만, 다른 사람 가게 일 경우, 접근 제한
        if("ROLE_OWNER".equals(role)&&(!username.equals(updateOrder.getStore().getOwner()
            .getUsername()))) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 본인 가게라면, status를 다음 단계로 변경 하도록 한다.
        if("ROLE_OWNER".equals(role)){
            //현재 OrderStatus를 다음 단계로 변경하고, 문자열로 변환하여 저장한다.
            status = updateOrder.getStatus().getNextStatus().name();
        }

        //그 외 마스터의 경우는 전달 받은 status를 사용하도록 한다.

        //Status만 변경하는 인스턴스 메서드를 호출하여 DB에 적용시킨다.
        updateOrder.statusUpdate(status);
    }

    /**
     * 주문 요청 사항 변경
     * @param orderId  주문 식별자
     * @param orderReq 변경된 주문요청 데이터
     * @param auth   로그인한 사용자 정보
     * @return  주문 응답객체
     */
    @Transactional
    public void updateOrderRequest(UUID orderId, OrderRequestDto orderReq, Authentication auth) {
        // 사용자 확인을 위해 인증 객체에서 username을 가져온다.
        String  username = (String) auth.getPrincipal();

        // 변경할 Order를 가져온다.
        Order updateOrder = orderRepository.findById(orderId).orElseThrow(()-> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        // 권한에 따른 Order 변경 작업을 위해 인증 객체에서 권한 목록을 가쟈온다.
        boolean isAllowed = auth.getAuthorities().stream()
            //특정 조건을 만족하는지 확인한다. = 사용자의 권한이 허용되는지 확인한다.
            .anyMatch(grantedAuthority -> {
                //
                String role =  grantedAuthority.getAuthority();
                // MASTER 권한은 Order 변경가능.
                if (role.equals("ROLE_MASTER")) {
                    return true;
                }
                //CUSTOMER 권한인데, 인증 객체의 username과 변경할 Order의 username과 다를 경우 Order 변경 불가능.
                if(role.equals("ROLE_CUSTOMER") && (!username.equals(updateOrder.getUser().getUsername()))) {
                    return false;
                }
                //CUSTOMER이면서 변경할 Order의 상태가 PENDING 이라면 Order 변경 가능.
                if (role.equals("ROLE_CUSTOMER") && updateOrder.getStatus() == OrderStatus.PENDING) {
                    return true;
                }
                // OWNER는 주문 요청사항을 수정 못하므로 false를 반환.
                return false;
            });

        //OWNER가 접근하거나 CUSTOMER인데 다른 사람 주문에 접근한 경우 예외를 던진다.
        if(!isAllowed) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        //변경된 내용이 담긴 req를 통해 Store를 가져온다.
        Store store =  storeRepository.findById(orderReq.getStoreId()).orElseThrow(() ->new CustomException(ErrorCode.STORE_NOT_FOUND));
        //변경된 내용이 담긴 req를 통해 Address를 가져온다.
        Address address = addressRepository.findById(orderReq.getAddressId()).orElseThrow(() ->new CustomException(ErrorCode.STORE_NOT_FOUND));

        // Order 데이터 업데이트 (전체 필드 반영)
        updateOrder.update(orderReq,store,address);
    }


    /**
     * 주문 생성 - 주문 테이블만
     * @param orderRequestDto : 생성할 주문 데이터
     * @param username        : 사용자 식별자
     * @return                : 생성된 주문을 응답.
     */
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto, String username) {
        // username으로 User를 조회 한다.
        User user = userRepository.findById(username).orElseThrow(() ->
            new IllegalArgumentException("사용자 없음."));

        // 생성할 주문 데이터에서 store를 조회한다.
        Store store = storeRepository.findById(orderRequestDto.getStoreId()).orElseThrow(() ->
            new IllegalArgumentException("가게없음."));

        // 생성할 주문 데이터에서 address를 조회한다.
        Address address = addressRepository.findById(orderRequestDto.getAddressId()).orElseThrow(() ->
            new IllegalArgumentException("주소없음."));

        // 생성할 주문 데이터 안에서 생성할 주문 메뉴 리스트를 추출.
        List<OrderItemRequestDto> items = orderRequestDto.getItems();

        // 주문 메뉴 리스트에서 menuId만 추출.
        List<UUID> menuIds = items.stream()
            .map(OrderItemRequestDto::getMenuId)
            .toList();

        // N개의 menuId를 DB에 요청하여 N개의 메뉴 리스트를 조회 - 안에 금액이 들어있음.
        List<Menu> menus = menuRepository.findAllById(menuIds);

        // 내부적으로 이중for문을 피하기 위해 메뉴 리스트를 map으로 변환.
        Map<UUID,Menu> menuMap = new HashMap<>();
        for (Menu menu : menus) {
            menuMap.put(menu.getMenuId(), menu);
        }

        // 총 주문 금액을 계산하기 위한 변수.
        Integer totalPrice = 0;

        //생성할 주문메뉴 리스트를 순회하면서
        for (OrderItemRequestDto item : items) {

            //메뉴 리스트 중 1개를 뽑아서
            Menu orderedMenu = menuMap.get(item.getMenuId());

            // 메뉴별 가격 * 수량을 계산하여 총 주문 금액을 계산한다.
            totalPrice += orderedMenu.getPrice() * item.getQuantity();
        }

        // 새로운 Order 객체로 변환
        Order newOrder = Order.createOrder(user
            ,store
            ,address
            ,orderRequestDto.getOrderType()
            ,totalPrice
            ,orderRequestDto.getRequest());

        orderRepository.save(newOrder);
        return OrderResponseDto.from(newOrder);
    }
}
