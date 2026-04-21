package com.sparta.deliveryorderplatform.menu.controller;

import com.sparta.deliveryorderplatform.menu.service.MenuServcie;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MenuController {

    private final MenuServcie menuServcie;

}
