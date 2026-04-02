package com.aman.project.airBnbApp.service;

import com.aman.project.airBnbApp.dto.BookingDto;
import com.aman.project.airBnbApp.dto.BookingRequest;
import com.aman.project.airBnbApp.dto.GuestDto;
import java.util.List;

public interface BookingService {
	BookingDto initialiseBooking(BookingRequest bookingRequest);

	BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);
}
