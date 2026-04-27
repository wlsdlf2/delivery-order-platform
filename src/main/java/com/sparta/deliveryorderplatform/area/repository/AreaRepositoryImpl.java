package com.sparta.deliveryorderplatform.area.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.deliveryorderplatform.area.dto.AreaSearchDTO;
import com.sparta.deliveryorderplatform.area.entity.Area;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.sparta.deliveryorderplatform.area.entity.QArea.area;

@Repository
@RequiredArgsConstructor
public class AreaRepositoryImpl implements AreaRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Area> searchAreas(AreaSearchDTO searchDTO, Pageable pageable) {
        List<Area> list = queryFactory
            .selectFrom(area)
            .where(
                containsKeyword(searchDTO.getKeyword()),
                isActiveStatus(searchDTO.getIsActive()),
                area.deletedAt.isNull() // 삭제된 데이터를 노출하지 않도록 고정
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(area.createdAt.desc())
            .fetch();

        Long total = queryFactory
            .select(area.count())
            .from(area)
            .where(
                containsKeyword(searchDTO.getKeyword()),
                isActiveStatus(searchDTO.getIsActive()),
                area.deletedAt.isNull()
            )
            .fetchOne();

        return new PageImpl<>(list, pageable, total != null ? total : 0L);
    }

    // 통합검색 조건
    private BooleanExpression containsKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;

        // name or city or district -> %keyword% 검색
        return area.name.containsIgnoreCase(keyword)
            .or(area.city.containsIgnoreCase(keyword))
            .or(area.district.containsIgnoreCase(keyword));
    }

    // 활성화여부 조건
    private BooleanExpression isActiveStatus(Boolean isActive) {
        return isActive != null ? area.isActive.eq(isActive) : null;
    }

}
