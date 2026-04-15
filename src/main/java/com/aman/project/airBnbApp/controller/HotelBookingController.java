package com.aman.project.airBnbApp.controller;

import com.aman.project.airBnbApp.dto.BookingDto;
import com.aman.project.airBnbApp.dto.BookingRequest;
import com.aman.project.airBnbApp.dto.GuestDto;
import com.aman.project.airBnbApp.service.BookingService;
import java.util.List;
import java.util.Map;
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
		@RequestBody List<Long> guestIds,
		@PathVariable("bookingId") Long bookingId
	) {
		return ResponseEntity.ok(bookingService.addGuests(bookingId, guestIds));
	}

	@PostMapping("/{bookingId}/payments")
	public ResponseEntity<Map<String, String>> initiatePayment(@PathVariable("bookingId") Long bookingId) {
		String sessionUrl = bookingService.initiatePayments(bookingId);

		return ResponseEntity.ok(Map.of("sessionUrl", sessionUrl));
	}

	@PostMapping("/{bookingId}/cancel")
	public ResponseEntity<Void> cancelBooking(@PathVariable("bookingId") Long bookingId) {
		bookingService.cancelBooking(bookingId);

		return ResponseEntity.noContent().build();
	}

	/// for polling
	/// frontend can keep calling this for the payment status
	/// once the payment is marked to CONFIRMED then the iser can be
	/// redirected to some other page

	@PostMapping("/{bookingId}/status")
	public ResponseEntity<Map<String, String>> getBookingStatus(@PathVariable("bookingId") Long bookingId) {
		return ResponseEntity.ok(Map.of("status", bookingService.getBookingStatus(bookingId)));
	}
}
