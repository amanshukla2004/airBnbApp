package com.aman.project.airBnbApp.security;

import com.aman.project.airBnbApp.dto.LoginDto;
import com.aman.project.airBnbApp.dto.SignUpRequestDto;
import com.aman.project.airBnbApp.dto.UserDto;
import com.aman.project.airBnbApp.entity.User;
import com.aman.project.airBnbApp.entity.enums.Role;
import com.aman.project.airBnbApp.exception.ResourceNotFoundException;
import com.aman.project.airBnbApp.exception.UnAuthorisedException;
import com.aman.project.airBnbApp.repository.UserRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final ModelMapper modelMapper;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JWTService jwtService;

	public UserDto signUpUser(SignUpRequestDto signUpRequestDto) {
		User user = userRepository.findByEmail(signUpRequestDto.getEmail()).orElse(null);
		if (user != null) {
			throw new RuntimeException("User already exists with same email");
		}
		User newUser = modelMapper.map(signUpRequestDto, User.class);
		newUser.setRoles(Set.of(Role.GUEST));
		newUser.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));
		newUser = userRepository.save(newUser);

		return modelMapper.map(newUser, UserDto.class);
	}

	public UserDto signUpManager(SignUpRequestDto signUpRequestDto) {
		User user = userRepository.findByEmail(signUpRequestDto.getEmail()).orElse(null);
		if (user != null) {
			throw new RuntimeException("User already exists with same email");
		}
		User newUser = modelMapper.map(signUpRequestDto, User.class);
		newUser.setRoles(Set.of(Role.HOTEL_MANAGER));
		newUser.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));
		newUser = userRepository.save(newUser);

		return modelMapper.map(newUser, UserDto.class);
	}

	public String[] loginUser(LoginDto loginDto) {
		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
		);
		User user = (User) authentication.getPrincipal();
		if (!user.getRoles().contains(Role.GUEST)) {
			throw new UnAuthorisedException("Account type does not match the login portal");
		}
		String[] arr = new String[2];
		arr[0] = jwtService.generateAccessToken(user);
		arr[1] = jwtService.generateRefreshToken(user);
		return arr;
	}

	public String[] loginManager(LoginDto loginDto) {
		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
		);
		User user = (User) authentication.getPrincipal();
		if (!user.getRoles().contains(Role.HOTEL_MANAGER)) {
			throw new UnAuthorisedException("Account type does not match the login portal");
		}
		String[] arr = new String[2];
		arr[0] = jwtService.generateAccessToken(user);
		arr[1] = jwtService.generateRefreshToken(user);
		return arr;
	}

	public String refreshToken(String refreshToken) {
		Long id = jwtService.getUserIdFromToken(refreshToken);

		User user = userRepository
			.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
		String token = jwtService.generateAccessToken(user);
		return token;
	}
}
//TODO: add validation in SignUpRequestDto
