package com.aman.project.airBnbApp.controller;

import com.aman.project.airBnbApp.dto.ProfileUpdateRequestDto;
import com.aman.project.airBnbApp.dto.UserDto;
import com.aman.project.airBnbApp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/profile")
@RequiredArgsConstructor
public class ManagerProfileController {

    private final UserService userService;

    /**
     * Update the currently authenticated Hotel Manager's profile.
     * Accessible only by HOTEL_MANAGER due to WebSecurityConfig.
     */
    @PatchMapping
    public ResponseEntity<Void> updateManagerProfile(@RequestBody ProfileUpdateRequestDto profileUpdateRequestDto) {
        userService.updateProfile(profileUpdateRequestDto);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieve the currently authenticated Hotel Manager's profile.
     */
    @GetMapping
    public ResponseEntity<UserDto> getManagerProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }
}
