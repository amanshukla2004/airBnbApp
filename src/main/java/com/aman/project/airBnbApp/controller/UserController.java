package com.aman.project.airBnbApp.controller;

import static com.aman.project.airBnbApp.util.AppUtils.getCurrentUser;

import com.aman.project.airBnbApp.dto.BookingDto;
import com.aman.project.airBnbApp.dto.ProfileUpdateRequestDto;
import com.aman.project.airBnbApp.dto.UserDto;
import com.aman.project.airBnbApp.entity.User;
import com.aman.project.airBnbApp.service.BookingService;
import com.aman.project.airBnbApp.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final BookingService bookingService;

	@PatchMapping("/profile")
	public ResponseEntity<Void> updateProfile(@RequestBody ProfileUpdateRequestDto profileUpdateRequestDto) {
		userService.updateProfile(profileUpdateRequestDto);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/myBookings")
	public ResponseEntity<List<BookingDto>> getMyBookings() {
		return ResponseEntity.ok(bookingService.getMyBookings());
	}

	@GetMapping("/profile")
	public ResponseEntity<UserDto> getMyProfile() {
		return ResponseEntity.ok(userService.getMyProfile());
	}
}
