package com.aman.project.airBnbApp.service;

import com.aman.project.airBnbApp.dto.*;
import com.aman.project.airBnbApp.entity.Room;
import java.util.List;
import org.springframework.data.domain.Page;

public interface InventoryService {
	void initialiseRoomForAYear(Room room);

	void deleteAllInventories(Room room);

	Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest);

	List<InventoryDto> getAllInventoryByRoom(Long roomId);

	void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto);
}
