package com.sparta.deliveryorderplatform.category.repository;

import com.sparta.deliveryorderplatform.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Page<Category> findAllByNameContaining(String name, Pageable pageable);

    // 삭제되지 않은 정상데이터 전체 조회
    Page<Category> findAllByDeletedAtIsNull(Pageable pageable);
    Page<Category> findAllByDeletedAtIsNullAndNameContaining(String name, Pageable pageable);


}

