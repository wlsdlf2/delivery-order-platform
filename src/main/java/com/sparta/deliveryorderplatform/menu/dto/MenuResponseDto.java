package com.sparta.deliveryorderplatform.menu.dto;

import com.sparta.deliveryorderplatform.menu.entity.Menu;
import lombok.Getter;

import java.util.UUID;

@Getter
public class MenuResponseDto {

    String name;
    Integer price;
    String description;

    public MenuResponseDto(Menu menu) {
        this.name = menu.getName();
        this.price = menu.getPrice();
        this.description = menu.getDescription();
    }
}
