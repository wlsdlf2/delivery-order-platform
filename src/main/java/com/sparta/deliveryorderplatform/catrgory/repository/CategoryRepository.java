package com.sparta.deliveryorderplatform.catrgory.repository;

import com.sparta.deliveryorderplatform.catrgory.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    // 전체 조회
    Page<Category> findAll(Pageable pageable);
    Page<Category> findAllByNameContaining(String name, Pageable pageable);

    // 삭제되지 않은 정상데이터 전체 조회
    Page<Category> findAllByDeletedAtIsNull(Pageable pageable);
    Page<Category> findAllByDeletedAtIsNullAndNameContaining(String name, Pageable pageable);


}

