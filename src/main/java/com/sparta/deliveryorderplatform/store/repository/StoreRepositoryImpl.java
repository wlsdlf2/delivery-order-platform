package com.sparta.deliveryorderplatform.store.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.deliveryorderplatform.store.dto.StoreSearchDTO;
import com.sparta.deliveryorderplatform.store.entity.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

import static com.sparta.deliveryorderplatform.store.entity.QStore.store;

@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Store> searchStores(StoreSearchDTO searchDTO, Pageable pageable) {
        List<Store> list = queryFactory
                .selectFrom(store)
                .where(
                    visibilityByRole(searchDTO.getRole(), searchDTO.getUsername()),
                    containsKeyword(searchDTO.getKeyword()),
                    eqCategory(searchDTO.getCategoryId()),
                    eqArea(searchDTO.getAreaId()),
                    store.deletedAt.isNull()    // 삭제된 데이터를 노출하지 않도록 고정
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(store.createdAt.desc())
                .fetch();

        Long total = queryFactory
                .select(store.count())
                .from(store)
                .where(
                    visibilityByRole(searchDTO.getRole(), searchDTO.getUsername()),
                    containsKeyword(searchDTO.getKeyword()),
                    eqCategory(searchDTO.getCategoryId()),
                    eqArea(searchDTO.getAreaId()),
                    store.deletedAt.isNull()    // 삭제된 데이터를 노출하지 않도록 고정
                )
                .fetchOne();

        return new PageImpl<>(list, pageable, total != null ? total : 0L);
    }

    private BooleanExpression visibilityByRole(String role, String username) {
        if ("ROLE_OWNER".equals(role)) return store.owner.username.eq(username);
        if ("ROLE_CUSTOMER".equals(role)) return store.isHidden.isFalse();

        return null; // MASTER
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
