package com.sparta.deliveryorderplatform.area.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AreaSearchDTO {
    private String keyword;
    private Boolean isActive;   // 관리자만 선택 가능한 필터값 (null이면 전체, true면 활성, false면 비활성)
}
