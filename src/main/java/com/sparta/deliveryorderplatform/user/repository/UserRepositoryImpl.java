package com.sparta.deliveryorderplatform.user.repository;

import static com.sparta.deliveryorderplatform.user.entity.QUser.*;
import static org.springframework.util.StringUtils.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

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
				usernameEq(condition.username()),
				nicknameContains(condition.nickname()),
				roleEq(condition.role()),
				user.deletedAt.isNull() // 소프트 삭제 제외
			)
			.orderBy(user.createdAt.desc()) // 기본 정렬 (필요시 Pageable의 sort 적용 가능)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		// 2. 카운트 쿼리 (성능 최적화용)
		JPAQuery<Long> countQuery = queryFactory
			.select(user.count())
			.from(user)
			.where(
				usernameEq(condition.username()),
				nicknameContains(condition.nickname()),
				roleEq(condition.role()),
				user.deletedAt.isNull()
			);

		// 3. Page 객체 반환 (PageableExecutionUtils 사용 시 카운트 쿼리 최적화 가능)
		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}

	// --- 동적 조건 메서드 (BooleanExpression) ---

	private BooleanExpression usernameEq(String username) {
		return hasText(username) ? user.username.eq(username) : null;
	}

	private BooleanExpression nicknameContains(String nickname) {
		return hasText(nickname) ? user.nickname.contains(nickname) : null;
	}

	private BooleanExpression roleEq(UserRole role) {
		return role != null ? user.role.eq(role) : null;
	}
}
