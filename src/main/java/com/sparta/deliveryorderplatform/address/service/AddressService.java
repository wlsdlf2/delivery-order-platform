package com.sparta.deliveryorderplatform.address.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.deliveryorderplatform.address.dto.AddressCreateRequest;
import com.sparta.deliveryorderplatform.address.dto.AddressResponse;
import com.sparta.deliveryorderplatform.address.dto.AddressUpdateRequest;
import com.sparta.deliveryorderplatform.address.entity.Address;
import com.sparta.deliveryorderplatform.address.repository.AddressRepository;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.entity.UserRole;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressService {

	private final AddressRepository addressRepository;

	@Transactional
	public AddressResponse createAddress(AddressCreateRequest request, User user) {
		if (request.isDefault()) {
			disableDefaultAddress(user);
		}

		Address address = Address.createAddress(
			request.alias(),
			request.address(),
			request.detail(),
			request.zipCode(),
			request.isDefault(),
			user
		);

		return AddressResponse.from(addressRepository.save(address));
	}

	public Page<AddressResponse> getAddresses(User user, Pageable pageable) {
		Page<Address> addressPage;

		if (user.getRole() == UserRole.MASTER) {
			addressPage = addressRepository.findAll(pageable);
		} else {
			addressPage = addressRepository.findByUserAndDeletedAtIsNull(user, pageable);
		}

		return addressPage.map(AddressResponse::from);
	}

	public AddressResponse getAddress(UUID addressID, User user) {
		Address address = findById(addressID);

		checkPermission(address, user);

		return AddressResponse.from(address);
	}

	@Transactional
	public AddressResponse updateAddress(UUID addressId, AddressUpdateRequest request, User user) {
		Address address = findById(addressId);

		checkPermission(address, user);

		if (request.isDefault() && !address.isDefault()) {
			disableDefaultAddress(user);
		}

		address.updateAddress(
			request.alias(),
			request.address(),
			request.detail(),
			request.zipCode(),
			request.isDefault()
		);

		return AddressResponse.from(address);
	}

	@Transactional
	public void deleteAddress(UUID addressId, User user) {
		Address address = findById(addressId);

		if (user.getRole() != UserRole.MASTER && !address.getUser().getUsername().equals(user.getUsername())) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}

		address.softDelete(user.getUsername());
	}

	@Transactional
	public void setDefaultAddress(UUID addressId, User user) {
		Address address = findById(addressId);

		checkPermission(address, user);

		disableDefaultAddress(user);

		address.markAsDefault();
	}

	private Address findById(UUID addressId) {
		return addressRepository.findById(addressId)
			.orElseThrow(() -> new CustomException(ErrorCode.ADDRESS_NOT_FOUND));
	}

	private void checkPermission(Address address, User user) {
		if (user.getRole() == UserRole.MASTER) {
			return;
		}

		if (!address.getUser().getUsername().equals(user.getUsername())) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}
	}

	private void disableDefaultAddress(User user) {
		addressRepository.findByUserAndIsDefaultTrue(user)
			.ifPresent(Address::unmarkAsDefault);
	}
}
