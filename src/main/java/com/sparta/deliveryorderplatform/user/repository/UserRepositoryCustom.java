package com.sparta.deliveryorderplatform.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sparta.deliveryorderplatform.user.dto.UserSearchCondition;
import com.sparta.deliveryorderplatform.user.entity.User;

public interface UserRepositoryCustom {
	Page<User> searchUsers(UserSearchCondition condition, Pageable pageable);
}
