package com.aman.project.airBnbApp.service;

import com.aman.project.airBnbApp.dto.RoomDto;
import java.util.List;
import org.springframework.stereotype.Service;

public interface RoomService {
	RoomDto createNewRoom(Long HotelId, RoomDto roomDto);

	List<RoomDto> getAllRoomsInHotel(Long hotelId);

	RoomDto getRoomById(Long roomId);

	void deleteRoomById(Long roomId);

	RoomDto updateRoomById(Long hotelId, Long roomId, RoomDto roomDto);
}
