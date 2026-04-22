package com.sparta.deliveryorderplatform.menu.service;

import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.menu.dto.MenuRequestDto;
import com.sparta.deliveryorderplatform.menu.dto.MenuResponseDto;
import com.sparta.deliveryorderplatform.menu.entity.Menu;
import com.sparta.deliveryorderplatform.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuServcie {

    private final MenuRepository menuRepository;

    public MenuResponseDto getMenu(UUID menuId) {
        Menu menu = findMenuById(menuId);
        return new MenuResponseDto(menu);
    }

    public Page<MenuResponseDto> getMenuList(UUID storeId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return menuRepository.findByStore_StoreIdAndDeletedAtIsNull(storeId, pageRequest).map(MenuResponseDto::new);
    }

    @Transactional
    public void updateMenu(UUID menuId, MenuRequestDto menuRequestDto) {
        Menu menu = findMenuById(menuId);
        menu.update(menuRequestDto.getName(), menuRequestDto.getPrice(), menuRequestDto.getDescription());
    }

    private Menu findMenuById(UUID menuId) {
        return menuRepository.findById(menuId).orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));
    }
}