package com.aman.project.airBnbApp.service;

import com.aman.project.airBnbApp.dto.HotelDto;
import com.aman.project.airBnbApp.dto.HotelPriceDto;
import com.aman.project.airBnbApp.dto.HotelSearchRequest;
import com.aman.project.airBnbApp.entity.Hotel;
import com.aman.project.airBnbApp.entity.Inventory;
import com.aman.project.airBnbApp.entity.Room;
import com.aman.project.airBnbApp.repository.HotelMinPriceRepository;
import com.aman.project.airBnbApp.repository.InventoryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

	private final ModelMapper modelMapper;

	private final InventoryRepository inventoryRepository;

	private final HotelMinPriceRepository hotelMinPriceRepository;

	@Override
	public void initialiseRoomForAYear(Room room) {
		LocalDate today = LocalDate.now();
		LocalDate endDate = today.plusYears(1);

		for (; !today.isAfter(endDate); today = today.plusDays(1)) {
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

			inventoryRepository.save(inventory);
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
}
/*
Criteria for inventory:
startDate <= date <= endDate
city
availability: (totalCount - bookedCount) >= roomsCount

Group the response by room
and get the response by unique hotels

 */
