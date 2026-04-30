package com.sparta.deliveryorderplatform.order.service;

import com.sparta.deliveryorderplatform.address.entity.Address;
import com.sparta.deliveryorderplatform.address.repository.AddressRepository;
import com.sparta.deliveryorderplatform.global.common.PageResponse;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.menu.entity.Menu;
import com.sparta.deliveryorderplatform.menu.repository.MenuRepository;
import com.sparta.deliveryorderplatform.order.dto.OrdeStatusRequestDto;
import com.sparta.deliveryorderplatform.order.dto.OrderItemRequestDto;
import com.sparta.deliveryorderplatform.order.dto.OrderRequestDto;
import com.sparta.deliveryorderplatform.order.dto.OrderResponseDto;
import com.sparta.deliveryorderplatform.order.dto.OrderSearch;
import com.sparta.deliveryorderplatform.order.entity.Order;
import com.sparta.deliveryorderplatform.order.entity.OrderItem;
import com.sparta.deliveryorderplatform.order.entity.OrderStatus;
import com.sparta.deliveryorderplatform.order.repository.OrderRepository;
import com.sparta.deliveryorderplatform.store.entity.Store;
import com.sparta.deliveryorderplatform.store.repository.StoreRepository;
import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.entity.UserRole;
import com.sparta.deliveryorderplatform.user.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * storeId를 받아, 이 가게의 모든 주문이 COMPLETED or CANCLE이 되어 있는지 확인.
     *
     * @param storeId 확인할 가게 식별자
     * @return true/false
     */
    public boolean storeInOrderIsCompleted(UUID storeId) {
        //이 가게로의 주문 상태들을 확인해서 활성화된 것들이 하나라도 있으면 true를 반환
        boolean hasActiveOrders = orderRepository.existsByStore_IdAndStatusNotIn(storeId,
            List.of(OrderStatus.COMPLETED, OrderStatus.CANCEL));

        //활성화된 것이 하나라도 있으면 false, 전부 끝난 거라면 true를 반환하게 된다.
        return !hasActiveOrders;
    }


    /**
     * 주문 상세 조회
     *
     * @param orderId  조회할 주문 식별자
     * @param username 인증 객체
     * @param role     인증 객체
     * @return 상세 주문 내역 응답
     */
    public OrderResponseDto getOrder(UUID orderId, String username, UserRole role) {
        // 상세 조회할 Order 객체를 조회.
        Order detailOrder = orderRepository.findById(orderId).orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        //OWNER의 경우 자기 가게로의 주문이 아닌, 다른 가게의 주문을 상세 조회할 경우 접근 제한.
        if (role == UserRole.OWNER && !username.equals(detailOrder.getStore().getOwner().getUsername())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        //CUSTOMER의 경우 자기 주문이 아닌 남의 주문을 상세 조회 할 경우 접근 제한.
        if (role == UserRole.CUSTOMER && !username.equals(detailOrder.getUser().getUsername())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        //관리자의 경우 모든 주문의 접근이 가능하므로, 제한 안함.

        return OrderResponseDto.from(detailOrder);
    }

    /**
     * 전체 목록 조회
     *
     * @param page     페이징 조건
     * @param username 사용자 식별자
     * @param role     사용자 권한
     * @return
     */
    public PageResponse getAllOrders(UUID storeId, OrderStatus status, Pageable page, String username,
        UserRole role) {
        Page<Order> orderPage;

        //먼저 사용자의 권한을 확인한다.
        if(role == UserRole.OWNER){
            // 일단 가게 정보를 넣지 않았을 때의 null 처리를 한다.
            if(storeId == null) {
                throw new CustomException(ErrorCode.STORE_NOT_FOUND);
            }
            //먼저 자기 가게인지 인증한다.
            Store store = storeRepository.findById(storeId).orElseThrow(()-> new CustomException(ErrorCode.STORE_NOT_FOUND));
            //본인 가게가 아니라면 접근 제한한다.
            if(!username.equals(store.getOwner().getUsername())){
                throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
            }


            //검색 조건 중 주문 상태만 확인한다.
            //주문 상태가 없다면
            if(status == null) {
                //이 가게로의 모든 주문을 조회한다.
                orderPage = orderRepository.findAllByStore_Id(storeId,page);
            }else {
                //주문 상태가 있다면, 주문 상태에 따른 이 가게로의 주문을 조회한다.
                orderPage = orderRepository.findAllByStore_IdAndStatus(storeId, status,page);
            }
        }else if(role == UserRole.CUSTOMER){ //권한이 고객이라면
            if(status != null && storeId == null){ //검색 조건이 주문 상태만 있다면(가게 정보가 없는 경우)
                orderPage = orderRepository.findAllByUser_usernameAndStatus(username, status,page);
            }else if(status == null && storeId != null){// 검색 조건이 가게 정보만 있다면(주문 상태가 없는 경우)
                orderPage = orderRepository.findAllByUser_usernameAndStore_id(username, storeId,page);
            }else if(status == null && storeId == null) { // 둘다 없는 거라면
                orderPage = orderRepository.findAllByUser_username(username, page);
            }else { // 둘다 있는 거라면
                orderPage = orderRepository.findAllByUser_usernameAndStatusAndStore_id(username,status,storeId,page);
            }
        }else if(role == UserRole.MASTER) { // 권한이 관리자라면,
            // 검색 조건을 먼저 확인한다.
            if(status != null && storeId == null) { //주문 상태만 있다면
                orderPage = orderRepository.findAllByStatus(status,page);
            }else if(status == null && storeId != null) { // 특정 가게에 대한 조건이 있다면
                orderPage = orderRepository.findAllByStore_id(storeId,page);
            }else if(status == null && storeId == null) { // 딱히 검색 조건이 없다라면
                orderPage = orderRepository.findAll(page);
            } else { // 검색 조건이 둘다 있다라면,
                orderPage = orderRepository.findAllByStatusAndStore_id(status,storeId, page);
            }
        } else { // 추후에 추가될 권한의 경우, 접근 제한을 건다.
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        return PageResponse.of(orderPage.map(OrderResponseDto::from));
    }



    /**
     * 주문 취소
     *
     * @param orderId  취소할 주문
     * @param username 사용자 식별자
     * @param role     사용자 권한
     */
    @Transactional
    public void cancelOrder(UUID orderId, String username, UserRole role) {
        //취소할 주문
        Order cancelOrder = orderRepository.findById(orderId)
            .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        //CUSTOMER이지만, 본인 주문이 아닌 경우 접근 제한을 건다.
        if ("ROLE_CUSTOMER".equals(role) && (!username.equals(
            cancelOrder.getUser().getUsername()))) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        //로직 수행 : 주문 생성 시, 5분이내에만 주문 취소가 가능하다.

        //주문 취소 요청 시 현재 시간을 가져온다.
        LocalDateTime now = LocalDateTime.now();
        //취소할 주문 건의 주문 생성 시간
        LocalDateTime orderTime = cancelOrder.getCreatedAt();

        //now와 orderTime의 차이를 시,분,초로 계산한다.
        Duration duration = Duration.between(orderTime, now);

        //전체 차이를 분 단위로 변환하여, 5보다 클 경우 예외를 던진다.
        if (duration.toMinutes() > 5) {
            throw new CustomException(ErrorCode.CANCEL_TIME_OUT);
        }

        //모든 단계를 통과하면, 정상적으로 주문 상태를 취소 상태로 변경한다.
        cancelOrder.statusUpdate(OrderStatus.CANCEL);
    }

    /**
     * 주문 삭제 - MASTER만
     *
     * @param orderId  삭제할 주문
     * @param username 사용자 식별자
     * @param role     사용자 권한
     * @return 삭제된 주문
     */
    @Transactional
    public OrderResponseDto deleteOrder(UUID orderId, String username, UserRole role) {
        //삭제할 Order를 가져온다.
        Order deleteOrder = orderRepository.findById(orderId)
            .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        //Order 엔티티의 삭제 인스턴스 메서드를 호출하여 삭제한다.
        deleteOrder.softDelete(username);
        return OrderResponseDto.from(deleteOrder);
    }

    /**
     * 주문 상태 변경 - OWNER와 MASTER가 할 수 있음.
     *
     * @param orderId  변경할 현재 Order Id
     * @param status   변경할 status 값
     * @param username 사용자 식별자
     * @param role     사용자 권한
     */
    @Transactional
    public void updateOrderStatus(UUID orderId, OrdeStatusRequestDto status, String username, UserRole role) {
        //변경할 현재 Order를 가져온다.
        Order updateOrder = orderRepository.findById(orderId)
            .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        // 사용자 권한이 가게 주인이지만, 다른 사람 주문에 접근했을 때에만 접근 제한을 설정한다.
        if (role == UserRole.OWNER && (!username.equals(
            updateOrder.getStore().getOwner().getUsername()))) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        // 다음 사용자 권한이 가게 주인일 때만, 순차적으로 주문 상태를 변경 시켜 status에 저장한다.
        if (role == UserRole.OWNER) {
            OrderStatus ownerStatus = updateOrder.getStatus().getNextStatus();
            updateOrder.statusUpdate(ownerStatus);
        } else {
            // 다음 사용자 권한이 관리자인 경우에는 전달 받은 status로 변경한다.
            updateOrder.statusUpdate(status.getStatus());
        }

    }

    /**
     * 주문 요청 사항 변경
     *
     * @param orderId       주문 식별자
     * @param updateRequest 변경된 요청사항
     * @param username      로그인한 사용자 식별자
     * @param role          로그인한 사용자 권한
     */
    @Transactional
    public void updateOrderRequest(UUID orderId, String updateRequest, String username,
        UserRole role) {
        // 변결 시킬 Order를 가져옴.
        Order updateOrder = orderRepository.findById(orderId).orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        // 고객인데 본인 주문이 아니라면 접근 제한
        if (role == UserRole.CUSTOMER && (!username.equals(updateOrder.getUser().getUsername()))) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 고객인데, 주문 상태가 PENDING이 아니라면 접근 제한
        if (role == UserRole.CUSTOMER && (!OrderStatus.PENDING.equals(updateOrder.getStatus()))) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 권한 검사를 마쳤더라면, 주문 요청 사항을 변경한다.
        updateOrder.updateRequest(updateRequest);
    }


    /**
     * 주문 생성 - 주문 테이블만
     *
     * @param orderRequestDto : 생성할 주문 데이터
     * @param username        : 로그인한 사용자 식별자
     * @return : 생성된 주문을 응답.
     */
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto, String username) {
        // user 정보 가져옴.
        User orderUser = userRepository.findById(username)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        //store 정보 가져옴.
        Store orderStore = storeRepository.findById(orderRequestDto.getStoreId())
            .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
        //address 정보 가져옴.
        Address orderAddress = addressRepository.findById(orderRequestDto.getAddressId())
            .orElseThrow(() -> new CustomException(ErrorCode.ADDRESS_NOT_FOUND));

        // 총 주문 금액.
        Integer totlaPrice = 0;

        //Db에 저장 시킬 Order를 미리 만듦.
        Order newOrder = Order.createOrder(orderUser, orderStore, orderAddress,
            orderRequestDto.getOrderType(), totlaPrice, orderRequestDto.getRequest());

        //OrderRequestDto에서 OrderItemReqestDto를 꺼낸다.
        List<OrderItemRequestDto> orderItemsList = orderRequestDto.getItems();

        //OrderItemList에서 menuId를 가져와 리스트로 만든다.
        List<UUID> orderMenuIdList = orderItemsList.stream().map(OrderItemRequestDto::getMenuId)
            .collect(Collectors.toList());

        //가져온 menuId 전부를 DB에서 호출하여 Menu 리스트를 가져온다.
        List<Menu> orderMenuList = menuRepository.findAllById(orderMenuIdList);

        //이 Menu 리스트를 map으로 변환한다.
        Map<UUID, Menu> orderMenuMap = new HashMap<>();
        for (Menu menu : orderMenuList) {
            orderMenuMap.put(menu.getMenuId(), menu);
        }

        //이렇게 만든 Menu와 OrderItemRequestDto를 가지고, totalPrice와 OrderItem을 만들고 , Order에 저장한다.
        for (OrderItemRequestDto orderItemReq : orderItemsList) {
            // totalPrice를 계산한다.
            Menu orderMenu = orderMenuMap.get(orderItemReq.getMenuId());
            totlaPrice += orderMenu.getPrice() * orderItemReq.getQuantity();

            OrderItem orderItem = OrderItem.createOrderItem(newOrder, orderMenu, orderItemReq.getQuantity(), orderMenu.getPrice());
            newOrder.addOrderItem(orderItem);
        }
        newOrder.updateTotalPrice(totlaPrice);
        orderRepository.save(newOrder);

        return  OrderResponseDto.from(newOrder);
    }


}
