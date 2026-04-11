package com.aman.project.airBnbApp.service;

import com.aman.project.airBnbApp.dto.BookingDto;
import com.aman.project.airBnbApp.dto.BookingRequest;
import com.aman.project.airBnbApp.dto.GuestDto;
import com.stripe.model.Event;
import java.util.List;
import java.util.Map;

public interface BookingService {
	BookingDto initialiseBooking(BookingRequest bookingRequest);

	BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);

	String initiatePayments(Long bookingId);

	void capturePayment(Event event);

	void cancelBooking(Long bookingId);

	String getBookingStatus(Long bookingId);
}
