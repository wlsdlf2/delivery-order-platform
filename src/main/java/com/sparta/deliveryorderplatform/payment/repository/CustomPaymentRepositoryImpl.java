package com.sparta.deliveryorderplatform.payment.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.deliveryorderplatform.payment.entity.Payment;
import com.sparta.deliveryorderplatform.payment.entity.QPayment;
import com.sparta.deliveryorderplatform.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.querydsl.core.types.Order.DESC;


@RequiredArgsConstructor
public class CustomPaymentRepositoryImpl implements CustomPaymentRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Payment> findPaymentList(String username, String role, Pageable pageable) {

        QPayment payment = QPayment.payment;

        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        Sort sort = pageable.getSort();

        List<Payment> content = jpaQueryFactory
                .selectFrom(payment)
                .where(roleCondition(username, role))
                .offset((long) page * size)
                .limit(size)
                .orderBy(getSortCondition(sort))
                .fetch();

        Long total = Optional.ofNullable(jpaQueryFactory
                .select(payment.count())
                .from(payment)
                .where(roleCondition(username, role))
                .fetchOne()).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }

    private OrderSpecifier[] getSortCondition(final Sort sort) {
        final List<OrderSpecifier> orders = new ArrayList<>();

        return new OrderSpecifier[]{new OrderSpecifier(DESC, QPayment.payment.createdAt)};
    }

    private BooleanBuilder roleCondition(String username, String role) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(QPayment.payment.deletedAt.isNull());

        if (role.equals(UserRole.CUSTOMER.getAuthority())) {
            builder.and(QPayment.payment.username.eq(username));
        }

        return builder;
    }
}
