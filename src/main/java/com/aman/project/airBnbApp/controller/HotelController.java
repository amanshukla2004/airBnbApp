package com.aman.project.airBnbApp.controller;

import com.aman.project.airBnbApp.dto.HotelDto;
import com.aman.project.airBnbApp.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/hotels")
@RequiredArgsConstructor
@Slf4j
public class HotelController {

	private final HotelService hotelService; // using the interface not implementation

	@PostMapping
	public ResponseEntity<HotelDto> createNewHotel(@RequestBody HotelDto hotelDto) {
		log.info("Attempting to create a new hotel" + hotelDto.getName());
		HotelDto hotel = hotelService.createNewHotel(hotelDto);
		//return ResponseEntity.ok().body(hotel); -> means and next line difference
		return new ResponseEntity<>(hotel, HttpStatus.CREATED);
	}

	// get all the hotels of a hotel manager - later

	@GetMapping("/{hotelId}")
	public ResponseEntity<HotelDto> getHotelById(@PathVariable Long hotelId) {
		HotelDto hotel = hotelService.getHotelById(hotelId);
		return new ResponseEntity<>(hotel, HttpStatus.OK);
	}

	@PutMapping("/{hotelId}")
	public ResponseEntity<HotelDto> updateHotelById(@PathVariable Long hotelId, @RequestBody HotelDto hotelDto) {
		HotelDto hotel = hotelService.updateHotelById(hotelId, hotelDto);
		return ResponseEntity.ok(hotel);
	}

	@DeleteMapping("/{hotelId}")
	public ResponseEntity<Void> deleteHotelById(@PathVariable Long hotelId) {
		hotelService.deleteHotelById(hotelId);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{hotelId}")
	public ResponseEntity<Void> activateHotel(@PathVariable Long hotelId) {
		hotelService.activateHotel(hotelId);
		return ResponseEntity.noContent().build();
	}
}
