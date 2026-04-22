package com.sparta.deliveryorderplatform.area.repository;

import com.sparta.deliveryorderplatform.area.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AreaRepository extends JpaRepository<Area, UUID>, AreaRepositoryCustom {
    boolean existsByNameAndDeletedAtIsNull(String name);

    Optional<Area> findByIdAndDeletedAtIsNull(UUID id);
}
