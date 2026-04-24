package com.sparta.deliveryorderplatform.address.entity;

import java.util.UUID;

import com.sparta.deliveryorderplatform.global.entity.BaseAuditEntity;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_address")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE)
public class Address extends BaseAuditEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "address_id")
	private UUID id;

	private String alias; // 별칭(집, 회사 등)

	@Column(nullable = false)
	private String address; // 주소

	private String detail; // 상세 주소

	@Column(name = "zip_code")
	private String zipCode;

	@Column(name = "is_default")
	private Boolean isDefault;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	public static Address createAddress(String alias, String address, String detail, String zipCode, Boolean isDefault, User user) {
		if (address == null || address.isBlank()) throw new CustomException(ErrorCode.VALIDATION_ERROR);
		if (user == null) throw new CustomException(ErrorCode.USER_NOT_FOUND);

		return Address.builder()
			.alias(alias)
			.address(address)
			.detail(detail)
			.zipCode(zipCode)
			.isDefault(isDefault != null && isDefault)
			.user(user)
			.build();
	}

	public void updateAddress(String alias, String address, String detail, String zipCode, Boolean isDefault) {
		if (address == null || address.isBlank()) throw new CustomException(ErrorCode.VALIDATION_ERROR);

		this.alias = alias;
		this.address = address;
		this.detail = detail;
		this.zipCode = zipCode;
		this.isDefault = isDefault;
	}

	public void markAsDefault() {
		this.isDefault = true;
	}

	public void unmarkAsDefault() {
		this.isDefault = false;
	}
}
