package com.sparta.deliveryorderplatform.store.service;

import com.sparta.deliveryorderplatform.store.dto.StoreReqeustDTO;
import com.sparta.deliveryorderplatform.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;

    public StoreResponseDTO createStore(StoreReqeustDTO reqeustDTO) {

    }
}
