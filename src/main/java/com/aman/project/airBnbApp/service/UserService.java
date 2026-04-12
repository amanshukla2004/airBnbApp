package com.aman.project.airBnbApp.service;

import com.aman.project.airBnbApp.dto.ProfileUpdateRequestDto;
import com.aman.project.airBnbApp.dto.UserDto;
import com.aman.project.airBnbApp.entity.User;

public interface UserService {
	User getUserById(Long id);

	void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

	UserDto getMyProfile();
	//User getUserByEmail(String email);
}
