package com.sparta.deliveryorderplatform.address.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.sparta.deliveryorderplatform.address.dto.AddressCreateRequest;
import com.sparta.deliveryorderplatform.address.dto.AddressResponse;
import com.sparta.deliveryorderplatform.address.dto.AddressUpdateRequest;
import com.sparta.deliveryorderplatform.address.entity.Address;
import com.sparta.deliveryorderplatform.address.repository.AddressRepository;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.entity.UserRole;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

	@Mock AddressRepository addressRepository;
	@InjectMocks AddressService addressService;

	private User createCustomer(String username) {
		return User.createUser(username, "닉네임", username + "@example.com", "encodedPw", UserRole.CUSTOMER);
	}

	private User createMaster() {
		return User.createUser("master1", "관리자", "master@example.com", "encodedPw", UserRole.MASTER);
	}

	private Address createAddress(User user, boolean isDefault) {
		return Address.createAddress("집", "서울시 강남구", "101호", "06000", isDefault, user);
	}

	// ─── createAddress ───────────────────────────────────────────────────────

	@Test
	@DisplayName("배송지 생성 성공 - 기본 배송지가 아닌 경우 그대로 저장된다")
	void createAddress_nonDefault_savesAndReturns() {
		User user = createCustomer("user1234");
		AddressCreateRequest request = new AddressCreateRequest("집", "서울시 강남구", "101호", "06000", false);
		Address saved = createAddress(user, false);
		given(addressRepository.save(any())).willReturn(saved);

		AddressResponse result = addressService.createAddress(request, user);

		assertThat(result.address()).isEqualTo("서울시 강남구");
		assertThat(result.isDefault()).isFalse();
	}

	@Test
	@DisplayName("배송지 생성 성공 - 기본 배송지로 생성 시 기존 기본 배송지가 해제된다")
	void createAddress_withDefault_disablesPreviousDefault() {
		User user = createCustomer("user1234");
		Address existing = createAddress(user, true);
		given(addressRepository.findByUserAndIsDefaultTrueAndDeletedAtIsNull(user)).willReturn(Optional.of(existing));
		Address saved = createAddress(user, true);
		given(addressRepository.save(any())).willReturn(saved);

		AddressCreateRequest request = new AddressCreateRequest("회사", "서울시 종로구", "202호", "03000", true);
		addressService.createAddress(request, user);

		assertThat(existing.isDefault()).isFalse();
	}

	// ─── getAddresses ────────────────────────────────────────────────────────

	@Test
	@DisplayName("배송지 목록 조회 성공 - CUSTOMER는 본인 배송지만 조회된다")
	void getAddresses_customer_returnsOwnAddresses() {
		User user = createCustomer("user1234");
		Address address = createAddress(user, false);
		given(addressRepository.findByUserAndDeletedAtIsNull(user, PageRequest.of(0, 10)))
			.willReturn(new PageImpl<>(List.of(address)));

		Page<AddressResponse> result = addressService.getAddresses(user, PageRequest.of(0, 10));

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).address()).isEqualTo("서울시 강남구");
	}

	@Test
	@DisplayName("배송지 목록 조회 성공 - MASTER는 전체 배송지가 조회된다")
	void getAddresses_master_returnsAllAddresses() {
		User master = createMaster();
		User other = createCustomer("other999");
		Address a1 = createAddress(master, false);
		Address a2 = createAddress(other, false);
		given(addressRepository.findAll(PageRequest.of(0, 10)))
			.willReturn(new PageImpl<>(List.of(a1, a2)));

		Page<AddressResponse> result = addressService.getAddresses(master, PageRequest.of(0, 10));

		assertThat(result.getContent()).hasSize(2);
	}

	// ─── getAddress ──────────────────────────────────────────────────────────

	@Test
	@DisplayName("배송지 상세 조회 성공 - 본인이 조회한다")
	void getAddress_owner_returnsAddress() {
		User user = createCustomer("user1234");
		Address address = createAddress(user, false);
		UUID id = UUID.randomUUID();
		given(addressRepository.findById(id)).willReturn(Optional.of(address));

		AddressResponse result = addressService.getAddress(id, user);

		assertThat(result.address()).isEqualTo("서울시 강남구");
	}

	@Test
	@DisplayName("배송지 상세 조회 성공 - MASTER가 타인 배송지를 조회한다")
	void getAddress_master_returnsAnyAddress() {
		User master = createMaster();
		User owner = createCustomer("user1234");
		Address address = createAddress(owner, false);
		UUID id = UUID.randomUUID();
		given(addressRepository.findById(id)).willReturn(Optional.of(address));

		assertThatCode(() -> addressService.getAddress(id, master)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("배송지 상세 조회 실패 - 타인 배송지 조회 시 ACCESS_DENIED 예외 발생")
	void getAddress_otherUser_throwsAccessDenied() {
		User owner = createCustomer("user1234");
		User other = createCustomer("other999");
		Address address = createAddress(owner, false);
		UUID id = UUID.randomUUID();
		given(addressRepository.findById(id)).willReturn(Optional.of(address));

		assertThatThrownBy(() -> addressService.getAddress(id, other))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ACCESS_DENIED);
	}

	@Test
	@DisplayName("배송지 상세 조회 실패 - 존재하지 않는 배송지 시 ADDRESS_NOT_FOUND 예외 발생")
	void getAddress_notFound_throwsAddressNotFound() {
		User user = createCustomer("user1234");
		UUID id = UUID.randomUUID();
		given(addressRepository.findById(id)).willReturn(Optional.empty());

		assertThatThrownBy(() -> addressService.getAddress(id, user))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ADDRESS_NOT_FOUND);
	}

	// ─── updateAddress ───────────────────────────────────────────────────────

	@Test
	@DisplayName("배송지 수정 성공 - 본인이 수정한다")
	void updateAddress_owner_returnsUpdated() {
		User user = createCustomer("user1234");
		Address address = createAddress(user, false);
		UUID id = UUID.randomUUID();
		given(addressRepository.findById(id)).willReturn(Optional.of(address));

		AddressUpdateRequest request = new AddressUpdateRequest("회사", "서울시 종로구", "202호", "03000", false);
		AddressResponse result = addressService.updateAddress(id, request, user);

		assertThat(result.address()).isEqualTo("서울시 종로구");
		assertThat(result.alias()).isEqualTo("회사");
	}

	@Test
	@DisplayName("배송지 수정 성공 - 기본 배송지로 변경 시 기존 기본 배송지가 해제된다")
	void updateAddress_setDefault_disablesPreviousDefault() {
		User user = createCustomer("user1234");
		Address existing = createAddress(user, true);
		Address target = createAddress(user, false);
		UUID id = UUID.randomUUID();
		given(addressRepository.findById(id)).willReturn(Optional.of(target));
		given(addressRepository.findByUserAndIsDefaultTrueAndDeletedAtIsNull(user)).willReturn(Optional.of(existing));

		AddressUpdateRequest request = new AddressUpdateRequest("회사", "서울시 종로구", "202호", "03000", true);
		addressService.updateAddress(id, request, user);

		assertThat(existing.isDefault()).isFalse();
		assertThat(target.isDefault()).isTrue();
	}

	@Test
	@DisplayName("배송지 수정 실패 - 타인 배송지 수정 시 ACCESS_DENIED 예외 발생")
	void updateAddress_otherUser_throwsAccessDenied() {
		User owner = createCustomer("user1234");
		User other = createCustomer("other999");
		Address address = createAddress(owner, false);
		UUID id = UUID.randomUUID();
		given(addressRepository.findById(id)).willReturn(Optional.of(address));

		AddressUpdateRequest request = new AddressUpdateRequest("회사", "서울시 종로구", "202호", "03000", false);

		assertThatThrownBy(() -> addressService.updateAddress(id, request, other))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ACCESS_DENIED);
	}

	@Test
	@DisplayName("배송지 수정 실패 - 존재하지 않는 배송지 시 ADDRESS_NOT_FOUND 예외 발생")
	void updateAddress_notFound_throwsAddressNotFound() {
		User user = createCustomer("user1234");
		UUID id = UUID.randomUUID();
		given(addressRepository.findById(id)).willReturn(Optional.empty());

		AddressUpdateRequest request = new AddressUpdateRequest("회사", "서울시 종로구", "202호", "03000", false);

		assertThatThrownBy(() -> addressService.updateAddress(id, request, user))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ADDRESS_NOT_FOUND);
	}

	// ─── deleteAddress ───────────────────────────────────────────────────────

	@Test
	@DisplayName("배송지 삭제 성공 - 본인이 소프트 삭제되고 deletedAt이 설정된다")
	void deleteAddress_owner_softDeletes() {
		User user = createCustomer("user1234");
		Address address = createAddress(user, false);
		UUID id = UUID.randomUUID();
		given(addressRepository.findById(id)).willReturn(Optional.of(address));

		addressService.deleteAddress(id, user);

		assertThat(address.getDeletedAt()).isNotNull();
		assertThat(address.getDeletedBy()).isEqualTo("user1234");
	}

	@Test
	@DisplayName("배송지 삭제 성공 - MASTER가 타인 배송지를 소프트 삭제한다")
	void deleteAddress_master_softDeletes() {
		User master = createMaster();
		User owner = createCustomer("user1234");
		Address address = createAddress(owner, false);
		UUID id = UUID.randomUUID();
		given(addressRepository.findById(id)).willReturn(Optional.of(address));

		addressService.deleteAddress(id, master);

		assertThat(address.getDeletedAt()).isNotNull();
		assertThat(address.getDeletedBy()).isEqualTo("master1");
	}

	@Test
	@DisplayName("배송지 삭제 실패 - 타인 배송지 삭제 시 ACCESS_DENIED 예외 발생")
	void deleteAddress_otherUser_throwsAccessDenied() {
		User owner = createCustomer("user1234");
		User other = createCustomer("other999");
		Address address = createAddress(owner, false);
		UUID id = UUID.randomUUID();
		given(addressRepository.findById(id)).willReturn(Optional.of(address));

		assertThatThrownBy(() -> addressService.deleteAddress(id, other))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ACCESS_DENIED);
	}

	@Test
	@DisplayName("배송지 삭제 실패 - 존재하지 않는 배송지 시 ADDRESS_NOT_FOUND 예외 발생")
	void deleteAddress_notFound_throwsAddressNotFound() {
		User user = createCustomer("user1234");
		UUID id = UUID.randomUUID();
		given(addressRepository.findById(id)).willReturn(Optional.empty());

		assertThatThrownBy(() -> addressService.deleteAddress(id, user))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ADDRESS_NOT_FOUND);
	}

	// ─── setDefaultAddress ───────────────────────────────────────────────────

	@Test
	@DisplayName("기본 배송지 설정 성공 - 기존 기본 배송지 해제 후 새 배송지가 기본으로 설정된다")
	void setDefaultAddress_success_replacesDefault() {
		User user = createCustomer("user1234");
		Address prev = createAddress(user, true);
		Address target = createAddress(user, false);
		UUID id = UUID.randomUUID();
		given(addressRepository.findById(id)).willReturn(Optional.of(target));
		given(addressRepository.findByUserAndIsDefaultTrueAndDeletedAtIsNull(user)).willReturn(Optional.of(prev));

		addressService.setDefaultAddress(id, user);

		assertThat(prev.isDefault()).isFalse();
		assertThat(target.isDefault()).isTrue();
	}

	@Test
	@DisplayName("기본 배송지 설정 실패 - 타인 배송지 설정 시 ACCESS_DENIED 예외 발생")
	void setDefaultAddress_otherUser_throwsAccessDenied() {
		User owner = createCustomer("user1234");
		User other = createCustomer("other999");
		Address address = createAddress(owner, false);
		UUID id = UUID.randomUUID();
		given(addressRepository.findById(id)).willReturn(Optional.of(address));

		assertThatThrownBy(() -> addressService.setDefaultAddress(id, other))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ACCESS_DENIED);
	}

	@Test
	@DisplayName("기본 배송지 설정 실패 - 존재하지 않는 배송지 시 ADDRESS_NOT_FOUND 예외 발생")
	void setDefaultAddress_notFound_throwsAddressNotFound() {
		User user = createCustomer("user1234");
		UUID id = UUID.randomUUID();
		given(addressRepository.findById(id)).willReturn(Optional.empty());

		assertThatThrownBy(() -> addressService.setDefaultAddress(id, user))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ADDRESS_NOT_FOUND);
	}
}
