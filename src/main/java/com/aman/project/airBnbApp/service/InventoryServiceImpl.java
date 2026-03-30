package com.aman.project.airBnbApp.service;

import com.aman.project.airBnbApp.entity.Inventory;
import com.aman.project.airBnbApp.entity.Room;
import com.aman.project.airBnbApp.repository.InventoryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

	private final InventoryRepository inventoryRepository;

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
	public void deleteFutureInventories(Room room) {
		LocalDate today = LocalDate.now();
		inventoryRepository.deleteByDateAfterAndRoom(today, room);
	}
}
