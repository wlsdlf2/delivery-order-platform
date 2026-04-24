package com.sparta.deliveryorderplatform.store.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class StoreSearchDTO {
    private String keyword;    // 가게 이름 검색
    private UUID categoryId;   // 카테고리 필터
    private UUID areaId;       // 지역 필터

    // 서비스 레이어에서 권한별 필터링을 위해 사용할 필드
    private String role;
    private String username;
}
