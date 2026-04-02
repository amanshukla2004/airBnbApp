package com.aman.project.airBnbApp.dto;

import com.aman.project.airBnbApp.entity.Hotel;
import com.aman.project.airBnbApp.entity.Room;
import com.aman.project.airBnbApp.entity.User;
import com.aman.project.airBnbApp.entity.enums.BookingStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Data;

@Data
public class BookingDto {

	private Long id;
	//	private Hotel hotel;
	//	private Room room;
	//private User user;
	private Integer roomCount;
	private LocalDate checkInDate;
	private LocalDate checkOutDate;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private BookingStatus bookingStatus;
	private Set<GuestDto> guests;
}
