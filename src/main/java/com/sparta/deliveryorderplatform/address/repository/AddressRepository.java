package com.sparta.deliveryorderplatform.address.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.deliveryorderplatform.address.entity.Address;
import com.sparta.deliveryorderplatform.user.entity.User;

public interface AddressRepository extends JpaRepository<Address, UUID> {
	Page<Address> findByUserAndDeletedAtIsNull(User user, Pageable pageable);
	Page<Address> findAll(Pageable pageable);
	Optional<Address> findByUserAndIsDefaultTrueAndDeletedAtIsNull (User user);
}
