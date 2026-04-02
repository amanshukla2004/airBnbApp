package com.aman.project.airBnbApp.service;

import com.aman.project.airBnbApp.dto.HotelDto;
import com.aman.project.airBnbApp.dto.HotelInfoDto;
import com.aman.project.airBnbApp.entity.Hotel;

public interface HotelService {
	HotelDto createNewHotel(HotelDto hotelDto);

	HotelDto getHotelById(Long id);

	HotelDto updateHotelById(Long id, HotelDto hotelDto);

	void deleteHotelById(Long id);

	void activateHotel(Long hotelId);

	HotelInfoDto getHotelInfoById(Long hotelId);
}
