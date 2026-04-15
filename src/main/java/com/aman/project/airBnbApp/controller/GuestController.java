package com.aman.project.airBnbApp.controller;

import com.aman.project.airBnbApp.dto.GuestDto;
import com.aman.project.airBnbApp.entity.User;
import com.aman.project.airBnbApp.service.GuestService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing user guests.
 * Allows authenticated users to add, update, remove, and list their associated guests.
 */
@RestController
@RequestMapping("/guests")
@RequiredArgsConstructor
public class GuestController {

    private final GuestService guestService;

    /**
     * Add a new guest for the currently authenticated user.
     * @param guestDto The guest details
     * @param user The authenticated user
     * @return The created guest
     */
    @PostMapping
    public ResponseEntity<GuestDto> addGuest(
            @RequestBody GuestDto guestDto, 
            @AuthenticationPrincipal User user) {
        return new ResponseEntity<>(guestService.addGuest(guestDto, user), HttpStatus.CREATED);
    }

    /**
     * Retrieve all guests associated with the currently authenticated user.
     * @param user The authenticated user
     * @return List of guests
     */
    @GetMapping
    public ResponseEntity<List<GuestDto>> getAllGuests(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(guestService.getAllGuestsForUser(user));
    }

    /**
     * Update an existing guest.
     * @param guestId ID of the guest to update
     * @param guestDto The updated guest details
     * @param user The authenticated user
     * @return The updated guest
     */
    @PutMapping("/{guestId}")
    public ResponseEntity<GuestDto> updateGuest(
            @PathVariable Long guestId, 
            @RequestBody GuestDto guestDto, 
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(guestService.updateGuest(guestId, guestDto, user));
    }

    /**
     * Remove a guest from the user's account.
     * @param guestId ID of the guest to remove
     * @param user The authenticated user
     * @return 204 No Content
     */
    @DeleteMapping("/{guestId}")
    public ResponseEntity<Void> removeGuest(
            @PathVariable Long guestId, 
            @AuthenticationPrincipal User user) {
        guestService.removeGuest(guestId, user);
        return ResponseEntity.noContent().build();
    }
}
