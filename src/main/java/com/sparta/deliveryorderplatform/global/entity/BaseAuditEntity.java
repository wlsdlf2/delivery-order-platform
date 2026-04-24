package com.sparta.deliveryorderplatform.global.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditEntity {

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@CreatedBy
	@Column(nullable = false, updatable = false)
	private String createdBy;

	@LastModifiedDate
	@Column(nullable = false)
	private LocalDateTime updatedAt;

	@LastModifiedBy
	@Column(nullable = false)
	private String updatedBy;

	private LocalDateTime deletedAt;

	@Column(length = 100)
	private String deletedBy;

	public void softDelete(String username) {
		this.deletedAt = LocalDateTime.now();
		this.deletedBy = username;
	}
}
