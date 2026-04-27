package com.sparta.deliveryorderplatform.order.service;

import com.sparta.deliveryorderplatform.address.entity.Address;
import com.sparta.deliveryorderplatform.address.repository.AddressRepository;
import com.sparta.deliveryorderplatform.global.common.PageResponse;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.menu.entity.Menu;
import com.sparta.deliveryorderplatform.menu.repository.MenuRepository;
import com.sparta.deliveryorderplatform.order.dto.OrderItemRequestDto;
import com.sparta.deliveryorderplatform.order.dto.OrderRequestDto;
import com.sparta.deliveryorderplatform.order.dto.OrderResponseDto;
import com.sparta.deliveryorderplatform.order.dto.OrderSearch;
import com.sparta.deliveryorderplatform.order.entity.Order;
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

    @Autowired
    private OrderItemService orderItemService;

    /**
     * storeId를 받아, 이 가게의 모든 주문이 COMPLETED or CANCLE이 되어 있는지 확인.
     * @param storeId  확인할 가게 식별자
     * @return   true/false
     */
    public boolean storeInOrderIsCompleted(UUID storeId) {
        //이 가게로의 주문 상태들을 확인해서 활성화된 것들이 하나라도 있으면 true를 반환
        boolean hasActiveOrders = orderRepository.existsByStore_IdAndStatusNotIn(storeId, List.of(OrderStatus.COMPLETED, OrderStatus.CANCEL));

        //활성화된 것이 하나라도 있으면 false, 전부 끝난 거라면 true를 반환하게 된다.
        return !hasActiveOrders;
    }


    /**
     * 주문 상세 조회
     * @param orderId 조회할 주문 식별자
     * @param username 인증 객체
     * @param role     인증 객체
     * @return 상세 주문 내역 응답
     */
    public OrderResponseDto getOrder(UUID orderId, String username, UserRole role){
        // 상세 조회할 Order 객체를 조회.
        Order detailOrder = orderRepository.findById(orderId).orElseThrow(()-> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        //OWNER인 경우, 본인 가게로의 주문이 아니라면
        if("ROLE_OWNER".equals(role)&& !username.equals(detailOrder.getStore().getOwner().getUsername())) {
            throw  new CustomException(ErrorCode.UNAUTHORIZED_ACCESS); // 접근 제한.
        }

        //CUSTOMER인 경우, 자기의 주문이 아니라면
        if("ROLE_CUSTOMER".equals(role) && !username.equals(detailOrder.getUser().getUsername())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS); // 접근제한
        }

        //관리자의 경우 모든 주문의 접근이 가능하므로, 제한 안함.

        return OrderResponseDto.from(detailOrder);
    }

    /**
     * 전체 목록 조회
     * @param search   검색 조건 - 주문 상태, 가게
     * @param page     페이징 조건
     * @param username 사용자 식별자
     * @param role     사용자 권한
     * @return
     */
    public PageResponse getAllOrders(OrderSearch search, Pageable page, String username, UserRole role) {
        //페이징 처리할 Order 객체
        Page<Order> orderPage;

        //권한별로 확인하여, 주문 목록을 달리 보여준다.
        if("ROLE_OWNER".equals(role)) { //가게 주인인 경우
            orderPage = orderRepository.findAllByStore_Id(search.getStoreId(),page);  //자기 가게로의 주문 목록만 확인한다.
        } else if("ROLE_CUSTOMER".equals(role)) {
            if(search.getStatus() == null){ //사용자가 주문 상태를 선택하지 않았다면,
                orderPage = orderRepository.findAllByUser_username(username, page); // 자기가 했던 주문만 나오도록 한다.
            } else {
                //그게 아니라면, 나의 주문 중 내가 선택한 주문 상태 목록을 가져온다.
                orderPage = orderRepository.findAllByStatusAndUser_username(search.getStatus(),username,page);
            }
        } else {
            orderPage = orderRepository.findAll(page);
        }
        return PageResponse.of(orderPage.map(OrderResponseDto::from));
    }


    /**
     * 주문 취소
     * @param orderId  취소할 주문
     * @param username 사용자 식별자
     * @param role     사용자 권한
     */
    @Transactional
    public void cancleOrder(UUID orderId,String username, UserRole role){
        //취소할 주문
        Order cancleOrder = orderRepository.findById(orderId).orElseThrow(()->new CustomException(ErrorCode.ORDER_NOT_FOUND));

        //CUSTOMER이지만, 본인 주문이 아닌 경우 접근 제한을 건다.
        if("ROLE_CUSTOMER".equals(role) && (!username.equals(cancleOrder.getUser().getUsername()))) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        //로직 수행 : 주문 생성 시, 5분이내에만 주문 취소가 가능하다.

        //주문 취소 요청 시 현재 시간을 가져온다.
        LocalDateTime now =  LocalDateTime.now();
        //취소할 주문 건의 주문 생성 시간
        LocalDateTime orderTime = cancleOrder.getCreatedAt();

        //now와 orderTime의 차이를 시,분,초로 계산한다.
        Duration duration = Duration.between(orderTime,now);

        //전체 차이를 분 단위로 변환하여, 5보다 클 경우 예외를 던진다.
        if(duration.toMinutes() > 5) {
            throw new CustomException(ErrorCode.CANCEL_TIME_OUT);
        }
        String status = "CANCEL";
        //모든 단계를 통과하면, 정상적으로 주문 상태를 취소 상태로 변경한다.
        cancleOrder.statusUpdate(status);
    }

    /**
     * 주문 삭제 - MASTER만
     * @param orderId  삭제할 주문
     * @param username 사용자 식별자
     * @param role     사용자 권한
     * @return 삭제된 주문
     */
    @Transactional
    public OrderResponseDto deleteOrder(UUID orderId, String username, UserRole role) {
        //권한이 마스터가 아닌 경우 예외를 던진다.
        if(!"ROLE_MASTER".equals(role)){
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        //삭제할 Order를 가져온다.
        Order deleteOrder = orderRepository.findById(orderId).orElseThrow(()-> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        //Order 엔티티의 삭제 인스턴스 메서드를 호출하여 삭제한다.
        deleteOrder.softDelete(username);
        return OrderResponseDto.from(deleteOrder);
    }

    /**
     * 주문 상태 변경
     * - OWNER와 MASTER가 할 수 있음.
     * @param orderId 변경할 현재 Order Id
     * @param status  변경할 status 값
     * @param username 사용자 식별자
     * @param role 사용자 권한
     */
    @Transactional
    public void updateOrderStatus(UUID orderId, String status, String username, UserRole role){
        //변경할 현재 Order를 가져온다.
        Order updateOrder = orderRepository.findById(orderId).orElseThrow(()->new CustomException(ErrorCode.ORDER_NOT_FOUND));

        // 사용자 권한이 가게 주인이지만, 다른 사람 주문에 접근했을 때에만 접근 제한을 설정한다.
        if(role == UserRole.OWNER &&(!username.equals(updateOrder.getStore().getOwner().getUsername()))){
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        // 다음 사용자 권한이 가게 주인일 때만, 순차적으로 주문 상태를 변경 시켜 status에 저장한다.
        if(role == UserRole.OWNER) {
            status = updateOrder.getStatus().getNextStatus().name();
        }

        // 마스터 일 때는 전달 받은 status를 그대로 사용한다.
        updateOrder.statusUpdate(status);
    }

    /**
     * 주문 요청 사항 변경
     * @param orderId  주문 식별자
     * @param orderReq 변경된 주문요청 데이터
     * @param username 로그인한 사용자 식별자
     * @param role      로그인한 사용자 권한
     * @return  주문 응답객체
     */
    @Transactional
    public void updateOrderRequest(UUID orderId, OrderRequestDto orderReq, String username, UserRole role) {
        // 사용자 확인을 위해 인증 객체에서 username을 가져온다.

        // 변경할 Order를 가져온다.
        Order updateOrder = orderRepository.findById(orderId).orElseThrow(()-> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        // 고객인데 본인 주문이 아니라면 접근 제한
        if(role == UserRole.CUSTOMER && (!username.equals(updateOrder.getUser().getUsername()))) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 고객인데, 주문 상태가 PENDING이 아니라면 접근 제한
        if(role == UserRole.CUSTOMER && (!OrderStatus.PENDING.equals(updateOrder.getStatus()))) {
            throw new  CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
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
     * @param username : 로그인한 사용자 식별자
     * @return                : 생성된 주문을 응답.
     */
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto, String username) {

        // username으로 User를 조회 한다.
        User user = userRepository.findById(username).orElseThrow(() ->
            new CustomException(ErrorCode.USER_NOT_FOUND));

        // 생성할 주문 데이터에서 store를 조회한다.
        Store store = storeRepository.findById(orderRequestDto.getStoreId()).orElseThrow(() ->
            new CustomException(ErrorCode.STORE_NOT_FOUND));

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
