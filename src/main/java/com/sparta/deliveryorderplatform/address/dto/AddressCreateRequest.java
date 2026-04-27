package com.sparta.deliveryorderplatform.address.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressCreateRequest(
	String alias,
	@NotBlank
	String address,
	String detail,
	String zipCode,
	boolean isDefault
) {}
