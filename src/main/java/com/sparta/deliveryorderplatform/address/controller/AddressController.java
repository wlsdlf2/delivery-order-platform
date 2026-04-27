package com.sparta.deliveryorderplatform.address.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.deliveryorderplatform.address.dto.AddressCreateRequest;
import com.sparta.deliveryorderplatform.address.dto.AddressResponse;
import com.sparta.deliveryorderplatform.address.dto.AddressUpdateRequest;
import com.sparta.deliveryorderplatform.address.service.AddressService;
import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import com.sparta.deliveryorderplatform.global.common.PageResponse;
import com.sparta.deliveryorderplatform.user.security.UserDetailsImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

	private final AddressService addressService;

	// 배송지 생성
	@PostMapping
	public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
		@RequestBody @Valid AddressCreateRequest request,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		AddressResponse response = addressService.createAddress(request, userDetails.getUser());
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}

	// 배송지 목록 조회
	@GetMapping
	public ResponseEntity<ApiResponse<PageResponse<AddressResponse>>> getAddressList(
		@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {

		Page<AddressResponse> responses = addressService.getAddresses(userDetails.getUser(), pageable);
		return ResponseEntity.ok(ApiResponse.success(PageResponse.of(responses)));
	}

	// 배송지 상세 조회
	@GetMapping("/{addressId}")
	public ResponseEntity<ApiResponse<AddressResponse>> getAddressDetail(
		@PathVariable UUID addressId,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {

		AddressResponse response = addressService.getAddress(addressId, userDetails.getUser());
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	// 배송지 수정
	@PutMapping("/{addressId}")
	public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
		@PathVariable UUID addressId,
		@RequestBody @Valid AddressUpdateRequest request,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {

		AddressResponse response = addressService.updateAddress(addressId, request, userDetails.getUser());
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	// 배송지 삭제
	@DeleteMapping("/{addressId}")
	public ResponseEntity<ApiResponse<Void>> deleteAddress(
		@PathVariable UUID addressId,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		addressService.deleteAddress(addressId, userDetails.getUser());
		return ResponseEntity.ok(ApiResponse.success());
	}

	// 기본 배송지 설정
	@PatchMapping("/{addressId}/default")
	public ResponseEntity<ApiResponse<Void>> setDefaultAddress(
		@PathVariable UUID addressId,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		addressService.setDefaultAddress(addressId, userDetails.getUser());
		return ResponseEntity.ok(ApiResponse.success());
	}
}
