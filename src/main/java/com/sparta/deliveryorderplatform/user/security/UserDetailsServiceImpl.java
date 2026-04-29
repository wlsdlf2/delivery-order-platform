package com.sparta.deliveryorderplatform.user.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.repository.UserRepository;
import com.sparta.deliveryorderplatform.user.service.UserCacheService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;
	private final UserCacheService userCacheService;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userCacheService.get(username)
			.map(UserDetailsImpl::new)
			.orElseGet(() -> {
				User user = userRepository.findById(username)
					.orElseThrow(() -> new UsernameNotFoundException("Not Found " + username));
				if (user.getDeletedAt() != null) {
					throw new UsernameNotFoundException("Deleted user: " + username);
				}
				userCacheService.cache(user);
				return new UserDetailsImpl(user);
			});
	}
}
