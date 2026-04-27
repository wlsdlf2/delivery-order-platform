package com.sparta.deliveryorderplatform.ai.repository;

import com.sparta.deliveryorderplatform.ai.entity.Ai;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AiRepository extends JpaRepository<Ai, UUID> {
}
