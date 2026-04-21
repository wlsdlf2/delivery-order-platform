package com.sparta.deliveryorderplatform.menu.service;

import com.sparta.deliveryorderplatform.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuServcie {

    private final MenuRepository menuRepository;

}
