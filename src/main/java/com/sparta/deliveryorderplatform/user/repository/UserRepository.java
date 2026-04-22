package com.sparta.deliveryorderplatform.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sparta.deliveryorderplatform.user.entity.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

	boolean existsByEmail (@NotBlank(message = "이메일을 입력해주세요.") @Email String email);
}
