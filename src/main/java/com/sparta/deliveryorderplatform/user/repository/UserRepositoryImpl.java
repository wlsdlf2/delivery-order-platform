package com.sparta.deliveryorderplatform.user.repository;

import static com.sparta.deliveryorderplatform.user.entity.QUser.*;
import static org.springframework.util.StringUtils.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.deliveryorderplatform.user.dto.UserSearchCondition;
import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.entity.UserRole;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<User> searchUsers(UserSearchCondition condition, Pageable pageable) {
		// 1. 콘텐츠 조회용 쿼리
		List<User> content = queryFactory
			.selectFrom(user)
			.where(
				keywordSearch(condition.keyword()),
				roleEq(condition.role()),
				user.deletedAt.isNull()
			)
			.orderBy(orderSpecifiers(pageable))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		// 2. 카운트 쿼리 (성능 최적화용)
		JPAQuery<Long> countQuery = queryFactory
			.select(user.count())
			.from(user)
			.where(
				keywordSearch(condition.keyword()),
				roleEq(condition.role()),
				user.deletedAt.isNull()
			);

		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}

	// --- 동적 조건 메서드 (BooleanExpression) ---

	private BooleanExpression keywordSearch(String keyword) {
		if (!hasText(keyword)) return null;
		return user.username.eq(keyword).or(user.nickname.contains(keyword));
	}

	private BooleanExpression roleEq(UserRole role) {
		return role != null ? user.role.eq(role) : null;
	}

	// --- 정렬 ---

	@SuppressWarnings("rawtypes")
	private OrderSpecifier[] orderSpecifiers(Pageable pageable) {
		if (!pageable.getSort().isSorted()) {
			return new OrderSpecifier[]{user.createdAt.desc()};
		}
		return pageable.getSort().stream()
			.map(order -> {
				boolean asc = order.isAscending();
				return switch (order.getProperty()) {
					case "username" -> asc ? user.username.asc() : user.username.desc();
					case "nickname" -> asc ? user.nickname.asc() : user.nickname.desc();
					case "updatedAt" -> asc ? user.updatedAt.asc() : user.updatedAt.desc();
					default -> asc ? user.createdAt.asc() : user.createdAt.desc();
				};
			})
			.toArray(OrderSpecifier[]::new);
	}
}
