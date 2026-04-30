package com.sparta.deliveryorderplatform.payment;

import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.order.entity.Order;
import com.sparta.deliveryorderplatform.order.repository.OrderRepository;
import com.sparta.deliveryorderplatform.payment.dto.request.CreatePaymentRequest;
import com.sparta.deliveryorderplatform.payment.dto.request.UpdatePaymentStatusRequest;
import com.sparta.deliveryorderplatform.payment.dto.response.PaymentResponse;
import com.sparta.deliveryorderplatform.payment.entity.Payment;
import com.sparta.deliveryorderplatform.payment.entity.PaymentMethod;
import com.sparta.deliveryorderplatform.payment.repository.PaymentRepository;
import com.sparta.deliveryorderplatform.payment.service.PaymentService;
import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @InjectMocks
    PaymentService paymentService;

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    OrderRepository orderRepository;

    Payment payment;
    UUID orderId;
    User user;
    Order order;
    UUID paymentId;

    @BeforeEach
    void setUp() {
        payment = mock(Payment.class);
        orderId = UUID.randomUUID();
        user = mock(User.class);
        order = mock(Order.class);
        paymentId = UUID.randomUUID();
    }


    @Nested
    class createPaymentTest {

        @Test
        void createPayment_ORDER_NOT_FOUND_EXCEPTION() {
            // given
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            CreatePaymentRequest request = mock(CreatePaymentRequest.class);
            when(request.getPaymentMethod()).thenReturn(PaymentMethod.CARD);

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> paymentService.createPayment(orderId, request, user));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_NOT_FOUND);
        }

        @Test
        void createPayment_USER_MISMATCH_EXCEPTION() {
            // given
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

            CreatePaymentRequest request = mock(CreatePaymentRequest.class);

            when(request.getPaymentMethod()).thenReturn(PaymentMethod.CARD);

            User paymentUser = mock(User.class);
            when(paymentUser.getUsername()).thenReturn("customer1");

            when(order.getUser()).thenReturn(paymentUser);
            when(user.getUsername()).thenReturn("customer2");

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> paymentService.createPayment(orderId, request, user));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_USER_MISMATCH);
        }

        @Test
        void createPayment_DUPLICATION_EXCEPTION() {
            // given
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

            CreatePaymentRequest request = mock(CreatePaymentRequest.class);

            when(request.getPaymentMethod()).thenReturn(PaymentMethod.CARD);

            User paymentUser = mock(User.class);
            when(paymentUser.getUsername()).thenReturn("customer1");

            when(order.getUser()).thenReturn(paymentUser);
            when(user.getUsername()).thenReturn("customer1");

            when(paymentRepository.existsByOrder(order)).thenReturn(Boolean.TRUE);

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> paymentService.createPayment(orderId, request, user));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_ALREADY_EXISTS);
        }

        @Test
        void createPayment_AMOUNT_MISSMATCH_EXCEPTION() {
            // given
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

            CreatePaymentRequest request = mock(CreatePaymentRequest.class);

            when(request.getPaymentMethod()).thenReturn(PaymentMethod.CARD);

            User paymentUser = mock(User.class);
            when(paymentUser.getUsername()).thenReturn("customer1");

            when(order.getUser()).thenReturn(paymentUser);
            when(user.getUsername()).thenReturn("customer1");

            when(paymentRepository.existsByOrder(order)).thenReturn(Boolean.FALSE);

            when(order.getTotalPrice()).thenReturn(10000);
            when(request.getAmount()).thenReturn(1000);

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> paymentService.createPayment(orderId, request, user));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        @Test
        void createPayment_SUCCESS() {
            // given
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

            CreatePaymentRequest request = mock(CreatePaymentRequest.class);

            when(request.getPaymentMethod()).thenReturn(PaymentMethod.CARD);

            User paymentUser = mock(User.class);
            when(paymentUser.getUsername()).thenReturn("customer1");

            when(order.getUser()).thenReturn(paymentUser);
            when(user.getUsername()).thenReturn("customer1");

            when(paymentRepository.existsByOrder(order)).thenReturn(Boolean.FALSE);

            when(order.getTotalPrice()).thenReturn(10000);
            when(request.getAmount()).thenReturn(10000);

            // when
            PaymentResponse response = paymentService.createPayment(orderId, request, user);

            // then
            assertThat(response).isNotNull();
            verify(paymentRepository).save(any(Payment.class));
        }
    }

    @Nested
    class getPaymentList {

        @Test
        void getPaymentList_size_correction() {
            // given
            when(paymentRepository.findPaymentList(any(), any(), any())).thenReturn(Page.empty());

            // when
            paymentService.getPaymentList(0, 20, user, UserRole.CUSTOMER.getAuthority());

            // then
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(paymentRepository).findPaymentList(any(), any(), pageableCaptor.capture());
            assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
        }

        @Test
        void getPaymentList_default_sort() {
            // given
            when(paymentRepository.findPaymentList(any(), any(), any())).thenReturn(Page.empty());

            // when
            paymentService.getPaymentList(0, 20, user, UserRole.CUSTOMER.getAuthority());

            // then
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(paymentRepository).findPaymentList(any(), any(), pageableCaptor.capture());
            assertThat(pageableCaptor.getValue().getSort())
                    .isEqualTo(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
    }

    @Nested
    class getPaymentById {

        @Test
        void getPaymentById_PAYMENT_NOT_FOUND_EXCEPTION() {
            // given
            when(paymentRepository.findByIdAndDeletedAtIsNull(paymentId)).thenReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> paymentService.getPaymentById(paymentId, user, UserRole.CUSTOMER.getAuthority()));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_NOT_FOUND);
        }

        @Test
        void getPaymentById_customer_UNAUTHORIZED_ACCESS_EXCEPTION() {
            // given
            when(paymentRepository.findByIdAndDeletedAtIsNull(paymentId)).thenReturn(Optional.of(payment));

            User paymentUser = mock(User.class);
            when(paymentUser.getUsername()).thenReturn("customer1");

            when(payment.getUser()).thenReturn(paymentUser);
            when(user.getUsername()).thenReturn("customer2");

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> paymentService.getPaymentById(paymentId, user, UserRole.CUSTOMER.getAuthority()));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        @Test
        void getPaymentById_owner_UNAUTHORIZED_ACCESS_EXCEPTION() {
            // given
            when(paymentRepository.findByIdAndDeletedAtIsNull(paymentId)).thenReturn(Optional.of(payment));

            when(user.getUsername()).thenReturn("owner1");

            when(paymentRepository.findOwnerUsernameByPaymentId(paymentId)).thenReturn("owner2");

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> paymentService.getPaymentById(paymentId, user, UserRole.OWNER.getAuthority()));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        @Test
        void getPaymentById_customer_SUCCESS() {
            // given
            when(paymentRepository.findByIdAndDeletedAtIsNull(paymentId)).thenReturn(Optional.of(payment));

            when(payment.getUser()).thenReturn(user);
            when(user.getUsername()).thenReturn("customer1");
            when(payment.getOrder()).thenReturn(order);
            // when
            PaymentResponse response = paymentService.getPaymentById(paymentId, user, UserRole.CUSTOMER.getAuthority());

            // then
            assertThat(response).isNotNull();
        }

        @Test
        void getPaymentById_owner_SUCCESS() {
            // given
            when(paymentRepository.findByIdAndDeletedAtIsNull(paymentId)).thenReturn(Optional.of(payment));

            when(payment.getUser()).thenReturn(user);
            when(user.getUsername()).thenReturn("owner1");
            when(payment.getOrder()).thenReturn(order);
            when(paymentRepository.findOwnerUsernameByPaymentId(paymentId)).thenReturn("owner1");

            // when
            PaymentResponse response = paymentService.getPaymentById(paymentId, user, UserRole.OWNER.getAuthority());

            // then
            assertThat(response).isNotNull();
        }

        @Test
        void getPaymentById_master_SUCCESS() {
            // given
            when(paymentRepository.findByIdAndDeletedAtIsNull(paymentId)).thenReturn(Optional.of(payment));

            when(payment.getOrder()).thenReturn(order);
            when(payment.getUser()).thenReturn(user);

            // when
            PaymentResponse response = paymentService.getPaymentById(paymentId, user, UserRole.MASTER.getAuthority());

            // then
            assertThat(response).isNotNull();
        }
    }

    @Nested
    class updatePaymentStatus {

        @Test
        void updatePaymentStatus_PAYMENT_NOT_FOUND_EXCEPTION() {
            // given
            when(paymentRepository.findByIdAndDeletedAtIsNull(paymentId)).thenReturn(Optional.empty());

            UpdatePaymentStatusRequest request = mock(UpdatePaymentStatusRequest.class);

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> paymentService.updatePaymentStatus(paymentId, request));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_NOT_FOUND);
        }

        @Test
        void updatePaymentStatus_SUCCESS () {
            // given
            when(paymentRepository.findByIdAndDeletedAtIsNull(paymentId)).thenReturn(Optional.of(payment));

            UpdatePaymentStatusRequest request = mock(UpdatePaymentStatusRequest.class);

            when(payment.getOrder()).thenReturn(order);
            when(payment.getUser()).thenReturn(user);

            // when
            PaymentResponse response =  paymentService.updatePaymentStatus(paymentId, request);

            // then
            assertThat(response).isNotNull();
        }
    }

    @Nested
    class deletePayment {

        @Test
        void deletePayment_PAYMENT_NOT_FOUND_EXCEPTION() {
            // given
            when(paymentRepository.findByIdAndDeletedAtIsNull(paymentId)).thenReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> paymentService.deletePayment(paymentId, user));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_NOT_FOUND);
        }

        @Test
        void deletePayment_SUCCESS() {
            // given
            when(paymentRepository.findByIdAndDeletedAtIsNull(paymentId)).thenReturn(Optional.of(payment));

            // when
            paymentService.deletePayment(paymentId, user);

            // then
            verify(payment).softDelete(any());
        }
    }
}
