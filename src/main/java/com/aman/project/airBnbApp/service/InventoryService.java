package com.aman.project.airBnbApp.service;

import com.aman.project.airBnbApp.dto.HotelDto;
import com.aman.project.airBnbApp.dto.HotelPriceDto;
import com.aman.project.airBnbApp.dto.HotelSearchRequest;
import com.aman.project.airBnbApp.entity.Room;
import org.springframework.data.domain.Page;

public interface InventoryService {
	void initialiseRoomForAYear(Room room);

	void deleteAllInventories(Room room);

	Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest);
}
