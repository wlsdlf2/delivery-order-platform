package com.sparta.deliveryorderplatform.menu.service;

import com.sparta.deliveryorderplatform.ai.client.AiClient;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.menu.dto.MenuRequestDto;
import com.sparta.deliveryorderplatform.menu.dto.MenuResponseDto;
import com.sparta.deliveryorderplatform.menu.entity.Menu;
import com.sparta.deliveryorderplatform.menu.repository.MenuRepository;
import com.sparta.deliveryorderplatform.store.entity.Store;
import com.sparta.deliveryorderplatform.store.repository.StoreRepository;
import com.sparta.deliveryorderplatform.user.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
    private final AiClient aiClient;

    @Transactional
    public void createMenu(MenuRequestDto menuRequestDto, UUID storeId, UserDetailsImpl userDetails, String token) {
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        if (role.equals("ROLE_CUSTOMER")) throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);

        //삭제 되지 않은 store 만 검색
        Store store = storeRepository.findByIdAndDeletedAtIsNull(storeId).orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
        //본인 가게 인지 check
        if (role.equals("ROLE_OWNER") && !store.getOwner().getUsername().equals(userDetails.getUsername()))
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);

        String description;
        Menu menu = new Menu(menuRequestDto, store);

        //ai 사용시 내부 client를 통해 HTTP 호출
        if (menuRequestDto.getAiDescription()) {
            description = aiClient.generateDescription(menuRequestDto.getAiPrompt(), token);
            menu.setDescription(description);
        }

        menuRepository.save(menu);
    }


    public MenuResponseDto getMenu(UUID menuId, UserDetailsImpl userDetails) {
        String role = getRole(userDetails);

        //CUSTOMER 는 숨김 메뉴 조회 불가
        if (role.equals("ROLE_CUSTOMER")) {
            Menu menu = menuRepository.findByMenuIdAndDeletedAtIsNullAndIsHiddenFalse(menuId).orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));
            return new MenuResponseDto(menu);
        }

        Menu menu = findMenuById(menuId);
        return new MenuResponseDto(menu);
    }

    public Page<MenuResponseDto> getMenuList(UUID storeId, String keyword, String sortField, String sortDirection, UserDetailsImpl userDetails, Pageable pageable) {

        int size = pageable.getPageSize();
        if (size != 10 && size != 30 && size != 50) size = 10;

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), size, Sort.by(direction, sortField));

        String role = getRole(userDetails);

        //QueryDSL 필터링 변환 작업 필요
        if (keyword != null && !keyword.isBlank()) {
            //CUSTOMER 는 숨김 메뉴 조회 불가
            return role.equals("ROLE_CUSTOMER") ? menuRepository.findByStore_idAndNameContainingAndDeletedAtIsNullAndIsHiddenFalse(storeId, keyword, pageRequest).map(MenuResponseDto::new) :
                    menuRepository.findByStore_idAndNameContainingAndDeletedAtIsNull(storeId, keyword, pageRequest).map(MenuResponseDto::new);
        }
        return role.equals("ROLE_CUSTOMER") ? menuRepository.findByStore_idAndDeletedAtIsNullAndIsHiddenFalse(storeId, pageRequest).map(MenuResponseDto::new) :
                menuRepository.findByStore_idAndDeletedAtIsNull(storeId, pageRequest).map(MenuResponseDto::new);
    }

    @Transactional
    public void updateMenu(UUID menuId, MenuRequestDto menuRequestDto, UserDetailsImpl userDetails) {
        Menu menu = findMenuById(menuId);
        String role = getRole(userDetails);

        validateMenuAccess(menu, userDetails, role);

        menu.update(menuRequestDto.getName(), menuRequestDto.getPrice(), menuRequestDto.getDescription());
    }

    @Transactional
    public void deleteMenu(UUID menuId, UserDetailsImpl userDetails) {
        Menu menu = findMenuById(menuId);
        String role = getRole(userDetails);

        validateMenuAccess(menu, userDetails, role);

        menu.softDelete(userDetails.getUsername());
    }

    @Transactional
    public void patchMenuStatus(UUID menuId, Boolean isHidden, UserDetailsImpl userDetails) {
        Menu menu = findMenuById(menuId);
        String role = getRole(userDetails);

        validateMenuAccess(menu, userDetails, role);

        menu.updateStatus(isHidden);
    }

    //단순 조회, 권한은 이후 validateMenuAcces 메서드로 check
    private Menu findMenuById(UUID menuId) {
        return menuRepository.findByMenuIdAndDeletedAtIsNull(menuId).orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));
    }

    private String getRole(UserDetailsImpl userDetails) {
        return userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCESS_DENIED));
    }

    //접근 권한 check
    private void validateMenuAccess(Menu menu, UserDetailsImpl userDetails, String role) {
        switch (role) {
            //모든 권한 허용
            case "ROLE_MASTER":
                break;

            //본인이 작성하거나 수정한 메뉴만 가능
            case "ROLE_OWNER":
                String username = userDetails.getUsername();
                if (!username.equals(menu.getCreatedBy()) && !username.equals(menu.getUpdatedBy()))
                    throw new CustomException(ErrorCode.ACCESS_DENIED);
                break;

            default:
                throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }
}