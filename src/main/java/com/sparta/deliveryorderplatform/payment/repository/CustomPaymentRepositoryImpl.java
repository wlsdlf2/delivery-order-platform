package com.sparta.deliveryorderplatform.payment.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.deliveryorderplatform.order.entity.QOrder;
import com.sparta.deliveryorderplatform.payment.entity.Payment;
import com.sparta.deliveryorderplatform.payment.entity.QPayment;
import com.sparta.deliveryorderplatform.store.entity.QStore;
import com.sparta.deliveryorderplatform.user.entity.QUser;
import com.sparta.deliveryorderplatform.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@RequiredArgsConstructor
public class CustomPaymentRepositoryImpl implements CustomPaymentRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Payment> findPaymentList(String username, String role, Pageable pageable) {

        QPayment payment = QPayment.payment;
        QOrder order = QOrder.order;
        QStore store = QStore.store;

        // 공통 쿼리 뼈대
        JPAQuery<Payment> query = jpaQueryFactory
                .selectFrom(payment);

        // role별로 필요한 조인만 추가
        if (isOwner(role)) {
            query.join(payment.order, order)
                    .join(order.store, store);
        }

        // where 조건
        BooleanBuilder roleCondition = roleCondition(username, role, payment, store);

        List<Payment> content = query
                .where(roleCondition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getSortCondition(pageable.getSort()))
                .fetch();

        // count 쿼리
        var countQuery = jpaQueryFactory
                .select(payment.count())
                .from(payment);

        if (isOwner(role)) {
            countQuery.join(payment.order, order)
                    .join(order.store, store);
        }

        Long total = Optional.ofNullable(
                countQuery.where(roleCondition).fetchOne()
        ).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public String findOwnerUsernameByPaymentId(UUID paymentId) {

        QPayment payment = QPayment.payment;
        QOrder order = QOrder.order;
        QStore store = QStore.store;
        QUser user = QUser.user;

        String ownerUsername = jpaQueryFactory
                .select(user.username)
                .from(payment)
                .join(payment.order, order)
                .join(order.store, store)
                .join(store.owner, user)
                .where(payment.id.eq(paymentId))
                .fetchOne();

        return ownerUsername;
    }

    private OrderSpecifier[] getSortCondition(final Sort sort) {

        // Sort 객체에서 첫 번째 정렬 조건 꺼냄. 없으면 기본값 createdAt,DESC
        Sort.Order sortOrder = sort.stream().findFirst()
                .orElse(Sort.Order.desc("createdAt"));

        // QueryDSL의 Order로 변환
        com.querydsl.core.types.Order direction = sortOrder.isAscending() ?
                com.querydsl.core.types.Order.ASC :
                com.querydsl.core.types.Order.DESC;

        return new OrderSpecifier[]{new OrderSpecifier(direction, QPayment.payment.createdAt)};
    }

    private BooleanBuilder roleCondition(String username, String role,
                                         QPayment payment, QStore store) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(QPayment.payment.deletedAt.isNull());

        if (isCustomer(role)) {
            builder.and(payment.user.username.eq(username));
        }

        if (isOwner(role)) {
            builder.and(store.owner.username.eq(username));
        }

        return builder;
    }

    private boolean isCustomer(String role) {
        return UserRole.CUSTOMER.getAuthority().equals(role);
    }

    private boolean isOwner(String role) {
        return UserRole.OWNER.getAuthority().equals(role);
    }
}
