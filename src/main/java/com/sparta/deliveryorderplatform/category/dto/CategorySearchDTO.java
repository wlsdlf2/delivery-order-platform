package com.sparta.deliveryorderplatform.category.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategorySearchDTO {
    private String keyword;     // 카테고리명 키워드 검색
    private Boolean isAdmin;    // 관리자 여부(삭제된 데이터 포함 조회권한 구분)
}
