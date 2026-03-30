package com.aman.project.airBnbApp.service;

import com.aman.project.airBnbApp.dto.RoomDto;
import com.aman.project.airBnbApp.entity.Hotel;
import com.aman.project.airBnbApp.entity.Room;
import com.aman.project.airBnbApp.exception.ResourceNotFoundException;
import com.aman.project.airBnbApp.repository.HotelRepository;
import com.aman.project.airBnbApp.repository.RoomRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

	private final RoomRepository roomRepository;
	private final HotelRepository hotelRepository;
	private final ModelMapper modelMapper;
	private final InventoryService inventoryService;

	@Override
	public RoomDto createNewRoom(Long hotelId, RoomDto roomDto) {
		log.info("Creating a new room in hotel with ID {}", hotelId);
		Hotel hotel = hotelRepository
			.findById(hotelId)
			.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with Id " + hotelId));

		Room room = modelMapper.map(roomDto, Room.class);
		room.setHotel(hotel);
		room = roomRepository.save(room);
		//  TODO: create inventory as soon as room is created and if hotel is active (DONE))
		if (hotel.getActive()) {
			inventoryService.initialiseRoomForAYear(room);
		}
		return modelMapper.map(room, RoomDto.class);
	}

	@Override
	public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
		log.info("Getting all room in hotel with ID {}", hotelId);
		Hotel hotel = hotelRepository
			.findById(hotelId)
			.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with Id " + hotelId));

		return hotel
			.getRooms()
			.stream()
			.map(element -> modelMapper.map(element, RoomDto.class))
			.collect(Collectors.toList());
	}

	@Override
	public RoomDto getRoomById(Long roomId) {
		log.info("Getting the room with ID {}", roomId);
		Room room = roomRepository
			.findById(roomId)
			.orElseThrow(() -> new ResourceNotFoundException("Room not found with Id " + roomId));

		return modelMapper.map(room, RoomDto.class);
	}

	@Override
	@Transactional
	public void deleteRoomById(Long roomId) {
		log.info("Deleting the room with ID {}", roomId);
		Room room = roomRepository
			.findById(roomId)
			.orElseThrow(() -> new ResourceNotFoundException("Room not found with Id " + roomId));
		// TODO: some error , cant delete the rooms
		inventoryService.deleteAllInventories(room);
		roomRepository.deleteById(roomId);
	}
}
