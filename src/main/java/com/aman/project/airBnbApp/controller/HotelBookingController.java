package com.aman.project.airBnbApp.controller;

import com.aman.project.airBnbApp.dto.BookingDto;
import com.aman.project.airBnbApp.dto.BookingRequest;
import com.aman.project.airBnbApp.dto.GuestDto;
import com.aman.project.airBnbApp.service.BookingService;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/bookings")
public class HotelBookingController {

	private final BookingService bookingService;

	@PostMapping("/init")
	public ResponseEntity<BookingDto> initialiseBooking(@RequestBody BookingRequest bookingRequest) {
		return ResponseEntity.ok(bookingService.initialiseBooking(bookingRequest));
	}

	@PostMapping("/{bookingId}/addGuests")
	public ResponseEntity<BookingDto> addGuests(
		@RequestBody List<GuestDto> guestDtoList,
		@PathVariable("bookingId") Long bookingId
	) {
		return ResponseEntity.ok(bookingService.addGuests(bookingId, guestDtoList));
	}
}
