package com.aman.project.airBnbApp.controller;

import com.aman.project.airBnbApp.dto.RoomDto;
import com.aman.project.airBnbApp.service.RoomService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/hotels")
@RequiredArgsConstructor
public class RoomAdminController {

	private final RoomService roomService;

	@PostMapping("/{hotelId}/rooms")
	public ResponseEntity<RoomDto> createNewRoom(@PathVariable Long hotelId, @RequestBody RoomDto roomDto) {
		RoomDto room = roomService.createNewRoom(hotelId, roomDto);
		return new ResponseEntity<>(room, HttpStatus.CREATED);
	}

	@GetMapping("/{hotelId}/rooms")
	public ResponseEntity<List<RoomDto>> getAllRoomsInHotel(@PathVariable Long hotelId) {
		return ResponseEntity.ok(roomService.getAllRoomsInHotel(hotelId));
	}

	@GetMapping({ "/{hotelId}/rooms/{roomId}" })
	public ResponseEntity<RoomDto> getRoomById(@PathVariable Long roomId) {
		return ResponseEntity.ok(roomService.getRoomById(roomId));
	}

	// /{hotelId}/rooms
	@DeleteMapping({ "/{hotelId}/rooms/{roomId}" })
	public ResponseEntity<RoomDto> deleteRoomById(@PathVariable Long roomId) {
		roomService.deleteRoomById(roomId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@PutMapping("/{hotelId}/rooms/{roomId}")
	public ResponseEntity<RoomDto> updateRoomById(
		@PathVariable Long hotelId,
		@PathVariable Long roomId,
		@RequestBody RoomDto roomDto
	) {
		return ResponseEntity.ok(roomService.updateRoomById(hotelId, roomId, roomDto));
	}
}
