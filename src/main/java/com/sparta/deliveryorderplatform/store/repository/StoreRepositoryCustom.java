package com.sparta.deliveryorderplatform.store.repository;

import com.sparta.deliveryorderplatform.store.dto.StoreSearchDTO;
import com.sparta.deliveryorderplatform.store.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StoreRepositoryCustom {
    Page<Store> searchStores(StoreSearchDTO searchDTO, Pageable pageable);
}
