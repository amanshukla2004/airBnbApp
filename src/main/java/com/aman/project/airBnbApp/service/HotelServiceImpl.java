package com.aman.project.airBnbApp.service;

import static com.aman.project.airBnbApp.util.AppUtils.getCurrentUser;

import com.aman.project.airBnbApp.dto.HotelDto;
import com.aman.project.airBnbApp.dto.HotelInfoDto;
import com.aman.project.airBnbApp.dto.RoomDto;
import com.aman.project.airBnbApp.entity.Hotel;
import com.aman.project.airBnbApp.entity.Room;
import com.aman.project.airBnbApp.entity.User;
import com.aman.project.airBnbApp.exception.ResourceNotFoundException;
import com.aman.project.airBnbApp.exception.UnAuthorisedException;
import com.aman.project.airBnbApp.repository.HotelRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

	private final HotelRepository hotelRepository;
	private final ModelMapper modelMapper;
	private final InventoryService inventoryService;

	@Override
	public HotelDto createNewHotel(HotelDto hotelDto) {
		log.info("createNewHotel {}", hotelDto.getName());

		Hotel hotel = modelMapper.map(hotelDto, Hotel.class);
		hotel.setActive(false);

		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		hotel.setOwner(user);
		hotelRepository.save(hotel);
		log.info("created NewHotel with ID: {}", hotelDto.getId());

		return modelMapper.map(hotel, HotelDto.class);
	}

	@Override
	public HotelDto getHotelById(Long id) {
		log.info("getting the Hotel with ID: {}", id);
		Hotel hotel = hotelRepository
			.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with Id " + id));

		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (!user.equals(hotel.getOwner())) {
			throw new UnAuthorisedException("This user does not own this hotel with id: " + id);
		}

		return modelMapper.map(hotel, HotelDto.class);
	}

	@Override
	public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
		log.info("updating the Hotel with ID: {}", id);
		Hotel hotel = hotelRepository
			.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with Id " + id));
		// update
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (!user.equals(hotel.getOwner())) {
			throw new UnAuthorisedException("This user does not own this hotel with id: " + id);
		}
		modelMapper.map(hotelDto, hotel);
		hotel.setId(id);
		hotelRepository.save(hotel);
		return modelMapper.map(hotel, HotelDto.class);
	}

	@Transactional
	@Override
	public void deleteHotelById(Long id) {
		Hotel hotel = hotelRepository
			.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with Id " + id));
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (!user.equals(hotel.getOwner())) {
			throw new UnAuthorisedException("This user does not own this hotel with id: " + id);
		}

		hotelRepository.deleteById(id);
		for (Room room : hotel.getRooms()) {
			inventoryService.deleteAllInventories(room);
		}
	}

	@Override
	@Transactional
	public void activateHotel(Long id) {
		log.info("activating the Hotel with ID: {}", id);
		Hotel hotel = hotelRepository
			.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with Id " + id));
		hotel.setActive(true);
		//assuming doing once

		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (!user.equals(hotel.getOwner())) {
			throw new UnAuthorisedException("This user does not own this hotel with id: " + id);
		}

		for (Room room : hotel.getRooms()) {
			inventoryService.initialiseRoomForAYear(room);
		}
	}

	// public method
	@Override
	public HotelInfoDto getHotelInfoById(Long hotelId) {
		Hotel hotel = hotelRepository
			.findById(hotelId)
			.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with Id " + hotelId));
		List<RoomDto> rooms = hotel
			.getRooms()
			.stream()
			.map(element -> modelMapper.map(element, RoomDto.class))
			.collect(Collectors.toList());
		return new HotelInfoDto(modelMapper.map(hotel, HotelDto.class), rooms);
	}

	@Override
	public List<HotelDto> getAllHotels() {
		User user = getCurrentUser();
		log.info("getting all Hotels with ID: {}", user.getId());
		List<Hotel> hotels = hotelRepository.findByOwner(user);
		return hotels.stream().map(element -> modelMapper.map(element, HotelDto.class)).collect(Collectors.toList());
	}
}
