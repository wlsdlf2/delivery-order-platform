package com.sparta.deliveryorderplatform.menu.dto;

import com.sparta.deliveryorderplatform.menu.entity.Menu;
import lombok.Getter;

@Getter
public class MenuResponseDto {

    private final String name;
    private final Integer price;
    private final String description;

    public MenuResponseDto(Menu menu) {
        this.name = menu.getName();
        this.price = menu.getPrice();
        this.description = menu.getDescription();
    }
}
