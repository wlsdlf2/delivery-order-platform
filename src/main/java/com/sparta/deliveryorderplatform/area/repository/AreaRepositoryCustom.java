package com.sparta.deliveryorderplatform.area.repository;

import com.sparta.deliveryorderplatform.area.dto.AreaSearchDTO;
import com.sparta.deliveryorderplatform.area.entity.Area;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AreaRepositoryCustom {
    Page<Area> searchAreas(AreaSearchDTO searchDTO, Pageable pageable);
}
