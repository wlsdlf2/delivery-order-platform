package com.sparta.deliveryorderplatform.area.repository;

import com.sparta.deliveryorderplatform.area.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AreaRepository extends JpaRepository<Area, UUID> {
    boolean existsByName(String name);
}
