package com.aman.project.airBnbApp.controller;

import com.aman.project.airBnbApp.dto.LoginDto;
import com.aman.project.airBnbApp.dto.LoginResponseDto;
import com.aman.project.airBnbApp.dto.SignUpRequestDto;
import com.aman.project.airBnbApp.dto.UserDto;
import com.aman.project.airBnbApp.security.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	/**
	 * Register a standard User.
	 */
	@PostMapping("/user/register")
	public ResponseEntity<UserDto> registerUser(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {
		return new ResponseEntity<>(authService.signUpUser(signUpRequestDto), HttpStatus.CREATED);
	}

	/**
	 * Login as a standard User.
	 */
	@PostMapping("/user/login")
	public ResponseEntity<LoginResponseDto> loginUser(
		@Valid @RequestBody LoginDto loginDto,
		HttpServletResponse httpServletResponse
	) {
		String[] tokens = authService.loginUser(loginDto);
		Cookie cookie = new Cookie("refreshToken", tokens[1]);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		httpServletResponse.addCookie(cookie);
		return ResponseEntity.ok(new LoginResponseDto(tokens[0]));
	}

	/**
	 * Register a Hotel Manager.
	 */
	@PostMapping("/manager/register")
	public ResponseEntity<UserDto> registerManager(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {
		return new ResponseEntity<>(authService.signUpManager(signUpRequestDto), HttpStatus.CREATED);
	}

	/**
	 * Login as a Hotel Manager.
	 */
	@PostMapping("/manager/login")
	public ResponseEntity<LoginResponseDto> loginManager(
		@Valid @RequestBody LoginDto loginDto,
		HttpServletResponse httpServletResponse
	) {
		String[] tokens = authService.loginManager(loginDto);
		Cookie cookie = new Cookie("refreshToken", tokens[1]);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		httpServletResponse.addCookie(cookie);
		return ResponseEntity.ok(new LoginResponseDto(tokens[0]));
	}

	@PostMapping("/refresh")
	public ResponseEntity<LoginResponseDto> refresh(HttpServletRequest request) {
		String refreshToken = Arrays
			.stream(request.getCookies())
			.filter(cookie -> "refreshToken".equals(cookie.getName()))
			.findFirst()
			.map(Cookie::getValue)
			.orElseThrow(() -> new AuthenticationServiceException("Refresh token not found inside the Cookies"));

		String accessToken = authService.refreshToken(refreshToken);

		return ResponseEntity.ok(new LoginResponseDto(accessToken));
		//
		//
		//
		//
		//

	}
}
