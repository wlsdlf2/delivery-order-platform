package com.sparta.deliveryorderplatform.menu.service;

import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.menu.dto.MenuRequestDto;
import com.sparta.deliveryorderplatform.menu.dto.MenuResponseDto;
import com.sparta.deliveryorderplatform.menu.entity.Menu;
import com.sparta.deliveryorderplatform.menu.repository.MenuRepository;
import com.sparta.deliveryorderplatform.store.entity.Store;
import com.sparta.deliveryorderplatform.store.repository.StoreRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public void createMenu(MenuRequestDto menuRequestDto, UUID storeId, Authentication authentication, HttpServletRequest request) {
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        if (role.equals("ROLE_CUSTOMER")) throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);

        Store store = storeRepository.findById(storeId).orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        Menu menu = new Menu(menuRequestDto, store);
        menuRepository.save(menu);
    }

    public MenuResponseDto getMenu(UUID menuId) {
        //TODO sunny - 이후 권한 파라미터 수정 필요
        Menu menu = findMenuById(menuId, "user");
        return new MenuResponseDto(menu);
    }

    //TODO sunny - 이후 권한 파라미터 수정 필요
    public Page<MenuResponseDto> getMenuList(UUID storeId, int page, int size, String user) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (user.equals("user"))
            return menuRepository.findByStore_idAndDeletedAtIsNullAndIsHiddenFalse(storeId, pageRequest).map(MenuResponseDto::new);
        return menuRepository.findByStore_idAndDeletedAtIsNull(storeId, pageRequest).map(MenuResponseDto::new);
    }

    @Transactional
    public void updateMenu(UUID menuId, MenuRequestDto menuRequestDto) {
        //TODO sunny - 이후 권한 파라미터 수정 필요
        Menu menu = findMenuById(menuId, "user");
        menu.update(menuRequestDto.getName(), menuRequestDto.getPrice(), menuRequestDto.getDescription());
    }

    //TODO sunny - softDelete parameter 수정
    @Transactional
    public void deleteMenu(UUID menuId) {
        //TODO sunny - 이후 권한 파라미터 수정 필요
        Menu menu = findMenuById(menuId, "user");
        menu.softDelete("admin");
    }

    @Transactional
    public void patchMenuStatus(UUID menuId, Boolean isHidden) {
        //TODO sunny - 이후 권한 파라미터 수정 필요
        Menu menu = findMenuById(menuId, "user");
        menu.updateStatus(isHidden);
    }

    //TODO sunny - 이후 권한 파라미터 수정 필요
    private Menu findMenuById(UUID menuId, String user) {
        if (user.equals("user"))
            return menuRepository.findByMenuIdAndDeletedAtIsNullAndIsHiddenFalse(menuId).orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));
        return menuRepository.findByMenuIdAndDeletedAtIsNull(menuId).orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));
    }
}