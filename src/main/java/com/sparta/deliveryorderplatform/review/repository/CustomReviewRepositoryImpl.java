package com.sparta.deliveryorderplatform.review.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.deliveryorderplatform.review.dto.request.SearchReviewCondition;
import com.sparta.deliveryorderplatform.review.entity.QReview;
import com.sparta.deliveryorderplatform.review.entity.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.querydsl.core.types.Order.ASC;
import static com.querydsl.core.types.Order.DESC;

@RequiredArgsConstructor
public class CustomReviewRepositoryImpl implements CustomReviewRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Review> searchReviews(SearchReviewCondition condition, Pageable pageable) {

        QReview review = QReview.review;
        UUID storeId = condition.getStoreId();
        Integer rating = condition.getRating();

        List<Review> content = jpaQueryFactory
                .selectFrom(review)
                .where(
                        equalStoreId(storeId),
                        equalRating(rating),
                        isDeleted())
                .offset((long) pageable.getPageNumber() * pageable.getPageSize())
                .limit(pageable.getPageSize())
                .orderBy(getSortCondition(pageable.getSort()))
                .fetch();

        Long total = Optional.ofNullable(jpaQueryFactory
                .select(review.count())
                .from(review)
                .where(
                        equalStoreId(storeId),
                        equalRating(rating),
                        isDeleted())
                .fetchOne()).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression equalStoreId(UUID storeId) {
        if (storeId == null)
            return null;
        return QReview.review.store.id.eq(storeId);
    }

    private BooleanExpression equalRating(Integer rating) {
        if (rating == null)
            return null;

        return QReview.review.rating.eq(rating);
    }
    private BooleanExpression isDeleted() {
        return QReview.review.deletedAt.isNull();
    }

    private OrderSpecifier[] getSortCondition(final Sort sort) {
        final List<OrderSpecifier> orders = new ArrayList<>();

        if (sort.isEmpty()) {
            return new OrderSpecifier[]{new OrderSpecifier(DESC, QReview.review.createdAt)};
        }

        for (final Sort.Order sortOrder : sort) {
            addOrderSpecifierByCurrentSortCondition(sortOrder, orders);
        }

        return orders.toArray(new OrderSpecifier[0]);
    }

    private void addOrderSpecifierByCurrentSortCondition(final Sort.Order sortOrder, final List<OrderSpecifier> specifiers) {

        Order direction = DESC;
        if (sortOrder.isAscending())
            direction = ASC;

        final String orderTarget = sortOrder.getProperty();

        if (orderTarget.equals("rating")) {
            specifiers.add(new OrderSpecifier(direction, QReview.review.rating));
        } else {
            specifiers.add(new OrderSpecifier(direction, QReview.review.createdAt));
        }

    }
}
