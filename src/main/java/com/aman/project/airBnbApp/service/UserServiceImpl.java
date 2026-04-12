package com.aman.project.airBnbApp.service;

import static com.aman.project.airBnbApp.util.AppUtils.getCurrentUser;

import com.aman.project.airBnbApp.dto.ProfileUpdateRequestDto;
import com.aman.project.airBnbApp.dto.UserDto;
import com.aman.project.airBnbApp.entity.User;
import com.aman.project.airBnbApp.exception.ResourceNotFoundException;
import com.aman.project.airBnbApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

	private final UserRepository userRepository;
	private final ModelMapper modelMapper;

	@Override
	public User getUserById(Long id) {
		return userRepository
			.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
	}

	@Override
	public void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto) {
		//
		User user = getCurrentUser();
		//modelMapper.map(profileUpdateRequestDto, user);
		//
		if (profileUpdateRequestDto.getDateOfBirth() != null) {
			user.setDateOfBirth(profileUpdateRequestDto.getDateOfBirth());
		}
		if (profileUpdateRequestDto.getGender() != null) {
			user.setGender(profileUpdateRequestDto.getGender());
		}
		if (profileUpdateRequestDto.getName() != null) {
			user.setName(profileUpdateRequestDto.getName());
		}

		userRepository.save(user);
	}

	@Override
	public UserDto getMyProfile() {
		User user = getCurrentUser();
		log.info("Getting User by id {}", user.getId());
		return modelMapper.map(user, UserDto.class);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByEmail(username).orElse(null);
	}
}
