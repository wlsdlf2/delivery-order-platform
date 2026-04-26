//package com.sparta.deliveryorderplatform.orderTest;
//
//import com.sparta.deliveryorderplatform.order.entity.Order;
//import com.sparta.deliveryorderplatform.order.entity.OrderStatus;
//import com.sparta.deliveryorderplatform.order.practice.AddressRepository;
//import com.sparta.deliveryorderplatform.order.practice.StoreRepository;
//import com.sparta.deliveryorderplatform.order.repository.OrderRepository;
//import com.sparta.deliveryorderplatform.order.service.OrderService;
//import com.sparta.deliveryorderplatform.store.entity.Store;
//import java.util.Collections;
//import java.util.Optional;
//import java.util.UUID;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.User;
//
//@ExtendWith(MockitoExtension.class)
//class OrderServiceTest {
//
//    @InjectMocks
//    private OrderService orderService;
//
//    @Mock
//    private OrderRepository orderRepository;
//
//    @Mock
//    private StoreRepository storeRepository;
//
//    @Mock
//    private AddressRepository addressRepository;
//
//    @Mock
//    private Authentication auth;
//
//
//    @Test
//    @DisplayName("가게 주인은 본인 가게의 주문 상태를 다음 단계로 변경할 수 있다.")
//    void updateOrderStatus_Owner_Success() {
//        // 1. 준비 (Given)
//        UUID orderId = UUID.randomUUID();
//        String ownerName = "ownerUser";
//
//        User owner = User.builder().username(ownerName).build();
//        Store store = Store.builder().user(owner).build();
//        // 현재 상태 PENDING (다음 상태는 ACCEPTED라고 가정)
//        Order order = Order.builder().status(OrderStatus.PENDING).store(store).build();
//
//        given(auth.getPrincipal()).willReturn(ownerName);
//        given(auth.getAuthorities()).willAnswer(inv ->
//            Collections.singletonList(new SimpleGrantedAuthority("ROLE_OWNER")).iterator());
//        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
//
//        // 2. 실행 (When)
//        orderService.updateOrderStatus(orderId, "dummyStatus", auth);
//
//        // 3. 검증 (Then)
//        // OrderStatus 내부 로직에 따라 PENDING의 다음 단계인 ACCEPTED로 변경되었는지 확인
//        assertEquals(OrderStatus.ACCEPTED, order.getStatus());
//    }
//
//    @Test
//    @DisplayName("가게 주인이 타인의 가게 주문 상태를 변경하려 하면 예외가 발생한다.")
//    void updateOrderStatus_Owner_AccessDenied() {
//        // 1. 준비 (Given)
//        UUID orderId = UUID.randomUUID();
//        given(auth.getPrincipal()).willReturn("otherOwner"); // 로그인한 사용자
//        given(auth.getAuthorities()).willAnswer(inv ->
//            Collections.singletonList(new SimpleGrantedAuthority("ROLE_OWNER")).iterator());
//
//        User realOwner = User.builder().username("realOwner").build();
//        Store store = Store.builder().user(realOwner).build();
//        Order order = Order.builder().store(store).build();
//
//        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
//
//        // 2. 실행 및 검증 (When & Then)
//        assertThrows(AccessDeniedException.class, () -> {
//            orderService.updateOrderStatus(orderId, "ACCEPTED", auth);
//        });
//    }
//
//
//}
