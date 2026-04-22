package com.sparta.deliveryorderplatform.menu.service;

import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.menu.dto.MenuResponseDto;
import com.sparta.deliveryorderplatform.menu.entity.Menu;
import com.sparta.deliveryorderplatform.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuServcie {

    private final MenuRepository menuRepository;

    public MenuResponseDto getMenu(UUID menuId) {
        Menu menu = findMenuById(menuId);
        return new MenuResponseDto(menu);
    }

    public List<MenuResponseDto> getMenuList(UUID storeId) {
        return menuRepository.findByStore_StoreId(storeId).stream().map(MenuResponseDto::new).toList();
    }

    private Menu findMenuById(UUID menuId) {
        return menuRepository.findById(menuId).orElseThrow(() -> new CustomException(ErrorCode.VALIDATION_ERROR));
    }
}