package com.sparta.deliveryorderplatform.address.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.deliveryorderplatform.address.dto.AddressCreateRequest;
import com.sparta.deliveryorderplatform.address.dto.AddressResponse;
import com.sparta.deliveryorderplatform.address.dto.AddressUpdateRequest;
import com.sparta.deliveryorderplatform.address.service.AddressService;
import com.sparta.deliveryorderplatform.auth.jwt.JwtAuthenticationEntryPoint;
import com.sparta.deliveryorderplatform.auth.jwt.JwtTokenProvider;
import com.sparta.deliveryorderplatform.global.config.SecurityConfig;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.entity.UserRole;
import com.sparta.deliveryorderplatform.user.security.UserDetailsImpl;

@WebMvcTest(AddressController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class})
class AddressControllerTest {

	@Autowired MockMvc mockMvc;
	@Autowired ObjectMapper objectMapper;

	@MockBean AddressService addressService;
	@MockBean JwtTokenProvider jwtTokenProvider;
	@MockBean org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

	private Authentication authOf(User user) {
		UserDetailsImpl userDetails = new UserDetailsImpl(user);
		return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	}

	private Authentication customerAuth() {
		return authOf(User.createUser("user1234", "닉네임", "test@example.com", "encoded", UserRole.CUSTOMER));
	}

	private Authentication masterAuth() {
		return authOf(User.createUser("master1", "관리자", "master@example.com", "encoded", UserRole.MASTER));
	}

	private AddressResponse sampleResponse() {
		return AddressResponse.builder()
			.id(UUID.randomUUID())
			.alias("집")
			.address("서울시 강남구")
			.detail("101호")
			.zipCode("06000")
			.isDefault(false)
			.createdAt(LocalDateTime.now())
			.build();
	}

	// ─── POST /api/v1/addresses ──────────────────────────────────────────────

	@Test
	@DisplayName("배송지 생성 성공 - 인증된 사용자가 201을 반환한다")
	void createAddress_authenticated_returns201() throws Exception {
		given(addressService.createAddress(any(), any())).willReturn(sampleResponse());
		AddressCreateRequest request = new AddressCreateRequest("집", "서울시 강남구", "101호", "06000", false);

		mockMvc.perform(post("/api/v1/addresses")
				.with(authentication(customerAuth()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.message").value("SUCCESS"))
			.andExpect(jsonPath("$.data.address").value("서울시 강남구"));
	}

	@Test
	@DisplayName("배송지 생성 실패 - 미인증 요청 시 401을 반환한다")
	void createAddress_unauthenticated_returns401() throws Exception {
		AddressCreateRequest request = new AddressCreateRequest("집", "서울시 강남구", "101호", "06000", false);

		mockMvc.perform(post("/api/v1/addresses")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("배송지 생성 실패 - 주소 누락 시 400과 VALIDATION_ERROR 코드를 반환한다")
	void createAddress_blankAddress_returns400() throws Exception {
		AddressCreateRequest request = new AddressCreateRequest("집", "", "101호", "06000", false);

		mockMvc.perform(post("/api/v1/addresses")
				.with(authentication(customerAuth()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	// ─── GET /api/v1/addresses ───────────────────────────────────────────────

	@Test
	@DisplayName("배송지 목록 조회 성공 - 인증된 사용자가 200을 반환한다")
	void getAddressList_authenticated_returns200() throws Exception {
		given(addressService.getAddresses(any(), any())).willReturn(Page.empty());

		mockMvc.perform(get("/api/v1/addresses")
				.with(authentication(customerAuth())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("SUCCESS"));
	}

	@Test
	@DisplayName("배송지 목록 조회 실패 - 미인증 요청 시 401을 반환한다")
	void getAddressList_unauthenticated_returns401() throws Exception {
		mockMvc.perform(get("/api/v1/addresses"))
			.andExpect(status().isUnauthorized());
	}

	// ─── GET /api/v1/addresses/{addressId} ──────────────────────────────────

	@Test
	@DisplayName("배송지 상세 조회 성공 - 인증된 사용자가 200을 반환한다")
	void getAddressDetail_authenticated_returns200() throws Exception {
		given(addressService.getAddress(any(), any())).willReturn(sampleResponse());

		mockMvc.perform(get("/api/v1/addresses/{id}", UUID.randomUUID())
				.with(authentication(customerAuth())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.address").value("서울시 강남구"));
	}

	@Test
	@DisplayName("배송지 상세 조회 실패 - 미인증 요청 시 401을 반환한다")
	void getAddressDetail_unauthenticated_returns401() throws Exception {
		mockMvc.perform(get("/api/v1/addresses/{id}", UUID.randomUUID()))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("배송지 상세 조회 실패 - 서비스에서 ACCESS_DENIED 발생 시 403을 반환한다")
	void getAddressDetail_accessDenied_returns403() throws Exception {
		given(addressService.getAddress(any(), any()))
			.willThrow(new CustomException(ErrorCode.ACCESS_DENIED));

		mockMvc.perform(get("/api/v1/addresses/{id}", UUID.randomUUID())
				.with(authentication(customerAuth())))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
	}

	@Test
	@DisplayName("배송지 상세 조회 실패 - 존재하지 않는 배송지 시 401을 반환한다")
	void getAddressDetail_notFound_returns401() throws Exception {
		given(addressService.getAddress(any(), any()))
			.willThrow(new CustomException(ErrorCode.ADDRESS_NOT_FOUND));

		mockMvc.perform(get("/api/v1/addresses/{id}", UUID.randomUUID())
				.with(authentication(customerAuth())))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("ADDRESS_NOT_FOUND"));
	}

	// ─── PUT /api/v1/addresses/{addressId} ──────────────────────────────────

	@Test
	@DisplayName("배송지 수정 성공 - 인증된 사용자가 200을 반환한다")
	void updateAddress_authenticated_returns200() throws Exception {
		given(addressService.updateAddress(any(), any(), any())).willReturn(sampleResponse());
		AddressUpdateRequest request = new AddressUpdateRequest("회사", "서울시 종로구", "202호", "03000", false);

		mockMvc.perform(put("/api/v1/addresses/{id}", UUID.randomUUID())
				.with(authentication(customerAuth()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("SUCCESS"));
	}

	@Test
	@DisplayName("배송지 수정 실패 - 미인증 요청 시 401을 반환한다")
	void updateAddress_unauthenticated_returns401() throws Exception {
		AddressUpdateRequest request = new AddressUpdateRequest("회사", "서울시 종로구", "202호", "03000", false);

		mockMvc.perform(put("/api/v1/addresses/{id}", UUID.randomUUID())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("배송지 수정 실패 - 주소 누락 시 400과 VALIDATION_ERROR 코드를 반환한다")
	void updateAddress_blankAddress_returns400() throws Exception {
		AddressUpdateRequest request = new AddressUpdateRequest("회사", "", "202호", "03000", false);

		mockMvc.perform(put("/api/v1/addresses/{id}", UUID.randomUUID())
				.with(authentication(customerAuth()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	@Test
	@DisplayName("배송지 수정 실패 - 서비스에서 ACCESS_DENIED 발생 시 403을 반환한다")
	void updateAddress_accessDenied_returns403() throws Exception {
		willThrow(new CustomException(ErrorCode.ACCESS_DENIED))
			.given(addressService).updateAddress(any(), any(), any());
		AddressUpdateRequest request = new AddressUpdateRequest("회사", "서울시 종로구", "202호", "03000", false);

		mockMvc.perform(put("/api/v1/addresses/{id}", UUID.randomUUID())
				.with(authentication(customerAuth()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
	}

	// ─── DELETE /api/v1/addresses/{addressId} ───────────────────────────────

	@Test
	@DisplayName("배송지 삭제 성공 - 인증된 사용자가 200을 반환한다")
	void deleteAddress_authenticated_returns200() throws Exception {
		mockMvc.perform(delete("/api/v1/addresses/{id}", UUID.randomUUID())
				.with(authentication(customerAuth())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("SUCCESS"));
	}

	@Test
	@DisplayName("배송지 삭제 실패 - 미인증 요청 시 401을 반환한다")
	void deleteAddress_unauthenticated_returns401() throws Exception {
		mockMvc.perform(delete("/api/v1/addresses/{id}", UUID.randomUUID()))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("배송지 삭제 실패 - 서비스에서 ACCESS_DENIED 발생 시 403을 반환한다")
	void deleteAddress_accessDenied_returns403() throws Exception {
		willThrow(new CustomException(ErrorCode.ACCESS_DENIED))
			.given(addressService).deleteAddress(any(), any());

		mockMvc.perform(delete("/api/v1/addresses/{id}", UUID.randomUUID())
				.with(authentication(customerAuth())))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
	}

	// ─── PATCH /api/v1/addresses/{addressId}/default ────────────────────────

	@Test
	@DisplayName("기본 배송지 설정 성공 - 인증된 사용자가 200을 반환한다")
	void setDefaultAddress_authenticated_returns200() throws Exception {
		mockMvc.perform(patch("/api/v1/addresses/{id}/default", UUID.randomUUID())
				.with(authentication(customerAuth())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("SUCCESS"));
	}

	@Test
	@DisplayName("기본 배송지 설정 실패 - 미인증 요청 시 401을 반환한다")
	void setDefaultAddress_unauthenticated_returns401() throws Exception {
		mockMvc.perform(patch("/api/v1/addresses/{id}/default", UUID.randomUUID()))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("기본 배송지 설정 실패 - 서비스에서 ACCESS_DENIED 발생 시 403을 반환한다")
	void setDefaultAddress_accessDenied_returns403() throws Exception {
		willThrow(new CustomException(ErrorCode.ACCESS_DENIED))
			.given(addressService).setDefaultAddress(any(), any());

		mockMvc.perform(patch("/api/v1/addresses/{id}/default", UUID.randomUUID())
				.with(authentication(customerAuth())))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
	}
}
