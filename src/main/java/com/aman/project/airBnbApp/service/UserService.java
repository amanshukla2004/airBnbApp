package com.aman.project.airBnbApp.service;

import com.aman.project.airBnbApp.entity.User;

public interface UserService {
	User getUserById(Long id);
	//User getUserByEmail(String email);
}
