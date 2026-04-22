package com.sparta.deliveryorderplatform.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sparta.deliveryorderplatform.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

}
