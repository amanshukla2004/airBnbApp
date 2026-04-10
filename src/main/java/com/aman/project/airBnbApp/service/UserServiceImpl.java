package com.aman.project.airBnbApp.service;

import com.aman.project.airBnbApp.entity.User;
import com.aman.project.airBnbApp.exception.ResourceNotFoundException;
import com.aman.project.airBnbApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService, UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public User getUserById(Long id) {
		return userRepository
			.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByEmail(username).orElse(null);
	}
}
