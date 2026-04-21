package com.sparta.deliveryorderplatform.menu.repository;

import com.sparta.deliveryorderplatform.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID> {
}
