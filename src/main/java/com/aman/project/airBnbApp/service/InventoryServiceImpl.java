package com.aman.project.airBnbApp.service;

import static com.aman.project.airBnbApp.util.AppUtils.getCurrentUser;

import com.aman.project.airBnbApp.dto.*;
import com.aman.project.airBnbApp.entity.Hotel;
import com.aman.project.airBnbApp.entity.Inventory;
import com.aman.project.airBnbApp.entity.Room;
import com.aman.project.airBnbApp.entity.User;
import com.aman.project.airBnbApp.exception.ResourceNotFoundException;
import com.aman.project.airBnbApp.repository.HotelMinPriceRepository;
import com.aman.project.airBnbApp.repository.InventoryRepository;
import com.aman.project.airBnbApp.repository.RoomRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

	private final RoomRepository roomRepository;

	private final ModelMapper modelMapper;

	private final InventoryRepository inventoryRepository;

	private final HotelMinPriceRepository hotelMinPriceRepository;

	@Override
	public void initialiseRoomForAYear(Room room) {
		LocalDate today = LocalDate.now();
		LocalDate endDate = today.plusYears(1);

		// Fetch existing inventory dates in one query to prevent N+1 queries
		java.util.Set<LocalDate> existingDates = inventoryRepository.findByRoomOrderByDate(room).stream()
				.map(Inventory::getDate)
				.collect(Collectors.toSet());

		java.util.List<Inventory> inventoriesToSave = new java.util.ArrayList<>();

		for (; !today.isAfter(endDate); today = today.plusDays(1)) {
			if (existingDates.contains(today)) {
				continue;
			}
			Inventory inventory = Inventory
				.builder()
				.hotel(room.getHotel())
				.room(room)
				.bookedCount(0)
				.reservedCount(0)
				.date(today)
				.city(room.getHotel().getCity())
				.totalCount(room.getTotalCount())
				.price(room.getBasePrice())
				.surgeFactor(BigDecimal.ONE)
				.closed(false)
				.build();

			inventoriesToSave.add(inventory);
		}

		// Save all records in one batch trip to the database
		if (!inventoriesToSave.isEmpty()) {
			inventoryRepository.saveAll(inventoriesToSave);
		}
	}

	@Override
	public void deleteAllInventories(Room room) {
		log.info("Delete the inventories of room with id: {}", room.getId());

		inventoryRepository.deleteByRoom(room);
	}

	@Override
	public Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest) {
		log.info(
			"Searching hotels for {} city, from {} to {}",
			hotelSearchRequest.getCity(),
			hotelSearchRequest.getStartDate(),
			hotelSearchRequest.getEndDate()
		);
		Pageable pageable = PageRequest.of(hotelSearchRequest.getPage(), hotelSearchRequest.getSize());
		// we need to get all the hotels in the city, that have
		// at least one city available bw start and end date.

		long dateCount =
			ChronoUnit.DAYS.between(hotelSearchRequest.getStartDate(), hotelSearchRequest.getEndDate()) + 1;

		// business logic - 90 days
		Page<HotelPriceDto> hotelPage = hotelMinPriceRepository.findHotelWithAvailableInventory(
			hotelSearchRequest.getCity(),
			hotelSearchRequest.getStartDate(),
			hotelSearchRequest.getEndDate(),
			hotelSearchRequest.getRoomsCount(),
			dateCount,
			pageable
		);
		return hotelPage;
		/*
		Page<Hotel> hotelPage = inventoryRepository.findHotelWithAvailableInventory(
			hotelSearchRequest.getCity(),
			hotelSearchRequest.getStartDate(),
			hotelSearchRequest.getEndDate(),
			hotelSearchRequest.getRoomsCount(),
			dateCount,
			pageable
		);
		return hotelPage.map(element -> modelMapper.map(element, HotelDto.class));
		 */
	}

	@Override
	public List<InventoryDto> getAllInventoryByRoom(Long roomId) {
		log.info("Getting All inventory by room for room with id: {}", roomId);
		Room room = roomRepository
			.findById(roomId)
			.orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
		User user = getCurrentUser();
		if (!user.equals(room.getHotel().getOwner())) {
			throw new AccessDeniedException("You are not the owner of this room ");
		}
		//
		//
		return inventoryRepository
			.findByRoomOrderByDate(room)
			.stream()
			.map(element -> modelMapper.map(element, InventoryDto.class))
			.collect(Collectors.toList());
	}

	@Transactional
	@Override
	public void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto) {
		log.info(
			"updating All inventory by room for room with id: {} between date ranges: {} to {}",
			roomId,
			updateInventoryRequestDto.getStartDate(),
			updateInventoryRequestDto.getEndDate()
		);
		Room room = roomRepository
			.findById(roomId)
			.orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
		User user = getCurrentUser();
		if (!user.equals(room.getHotel().getOwner())) {
			throw new AccessDeniedException("You are not the owner of this room ");
		}
		//
		inventoryRepository.getInventoryAndLockBeforeUpdate(
			roomId,
			updateInventoryRequestDto.getStartDate(),
			updateInventoryRequestDto.getEndDate()
		);
		inventoryRepository.updateInventory(
			roomId,
			updateInventoryRequestDto.getStartDate(),
			updateInventoryRequestDto.getEndDate(),
			updateInventoryRequestDto.getClosed(),
			updateInventoryRequestDto.getSurgeFactor(),
			updateInventoryRequestDto.getPrice() // Passed the customized price
		);
	}

	@Override
	@Transactional
	public void updateBasePriceForFutureOpenInventories(Long roomId, BigDecimal price, LocalDate startDate) {
		log.info("Updating base price for future open inventories of room {} to {}", roomId, price);
		inventoryRepository.updateBasePriceForFutureOpenInventories(roomId, price, startDate);
	}

	@Override
	public void deleteHotelMinPriceEntries(Hotel hotel) {
		log.info("Deleting HotelMinPrice entries for hotel with id: {}", hotel.getId());
		hotelMinPriceRepository.deleteByHotel(hotel);
	}
}
/*
Criteria for inventory:
startDate <= date <= endDate
city
availability: (totalCount - bookedCount) >= roomsCount

Group the response by room
and get the response by unique hotels

 */
