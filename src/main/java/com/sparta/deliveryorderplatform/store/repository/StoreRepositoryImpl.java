package com.sparta.deliveryorderplatform.store.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.deliveryorderplatform.store.dto.StoreSearchDTO;
import com.sparta.deliveryorderplatform.store.entity.Store;
import com.sparta.deliveryorderplatform.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

import static com.sparta.deliveryorderplatform.store.entity.QStore.store;

@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Store> searchStores(StoreSearchDTO searchDTO, Pageable pageable) {
        // 공통 WHERE 절 조건 정의
        BooleanExpression[] predicates = {
            visibilityByRole(searchDTO.getRole(), searchDTO.getUsername()),
            containsKeyword(searchDTO.getKeyword()),
            eqCategory(searchDTO.getCategoryId()),
            eqArea(searchDTO.getAreaId()),
            store.deletedAt.isNull()    // 삭제된 데이터를 노출하지 않도록 고정
        };

        // 데이터 리스트 조회
        List<Store> list = queryFactory
                .selectFrom(store)
                // 연관된 엔티티 미리 가져오기 -> N+1 문제 방지
                .innerJoin(store.owner).fetchJoin()
                .innerJoin(store.category).fetchJoin()
                .innerJoin(store.area).fetchJoin()
                .where(predicates)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(store.createdAt.desc())
                .fetch();

        // 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(store.count())
                .from(store)
                .where(predicates);

        return PageableExecutionUtils.getPage(list, pageable, countQuery::fetchOne);
    }

    private BooleanExpression visibilityByRole(UserRole role, String username) {
        // MASTER -> 필터 없음
        if (UserRole.MASTER == role) return null;

        // OWNER -> 본인 가게만 조회
        if (UserRole.OWNER == role) return store.owner.username.eq(username);

        // 그외(CUSTOMER) -> 숨김 제외
        return store.isHidden.isFalse();
    }

    // 가게명 키워드
    private BooleanExpression containsKeyword(String keyword) {
        return StringUtils.hasText(keyword) ?  store.name.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression eqCategory(UUID categoryId) {
        return categoryId != null ? store.category.id.eq(categoryId) : null;
    }

    private BooleanExpression eqArea(UUID areaId) {
        return areaId != null ?  store.area.id.eq(areaId) : null;
    }
}
