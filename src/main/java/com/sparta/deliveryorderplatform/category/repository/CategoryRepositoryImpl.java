package com.sparta.deliveryorderplatform.category.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.deliveryorderplatform.category.dto.CategorySearchDTO;
import com.sparta.deliveryorderplatform.category.entity.Category;
import com.sparta.deliveryorderplatform.category.entity.QCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    QCategory qCategory = QCategory.category;

    @Override
    public Page<Category> searchCategories(CategorySearchDTO searchDTO, Pageable pageable) {
        // 데이터 조회 기본쿼리 생성
        JPAQuery<Category> query = queryFactory
            .selectFrom(qCategory)
            .where(
                containsKeyword(searchDTO.getKeyword()),
                isAccessible(searchDTO.getIsAdmin())
            )
            .orderBy(qCategory.createdAt.desc());

        // 페이징처리 여부
        if (pageable.isPaged()) {
            query.offset(pageable.getOffset())
                .limit(pageable.getPageSize());
        }

        List<Category> list = query.fetch();

        // 카운트 -> 페이징
        Long total = queryFactory
            .select(qCategory.count())
            .from(qCategory)
            .where(
                containsKeyword(searchDTO.getKeyword()),
                isAccessible(searchDTO.getIsAdmin())
            )
            .fetchOne();
        
        return new PageImpl<>(list, pageable, total);
    }

    private BooleanExpression containsKeyword(String keyword) {
        return StringUtils.hasText(keyword) ? qCategory.name.contains(keyword) : null;
    }

    private BooleanExpression isAccessible(Boolean isAdmin) {
        // 관리자가 아닌 일반사용자 -> 삭제되지 않은 데이터만 조회
        if (isAdmin == null || !isAdmin) {
            return qCategory.deletedAt.isNull();
        }

        // 관리자 -> 모든 데이터 조회가능(조건 없음)
        return null;
    }
}
