package com.sparta.deliveryorderplatform.area.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AreaSearchDTO {
    private String keyword;
    private Boolean isAdmin;
    private Boolean isActive;
}
