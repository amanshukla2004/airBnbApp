package com.aman.project.airBnbApp.dto;

import com.aman.project.airBnbApp.entity.Hotel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelPriceDto {

	private Hotel hotel;
	private Double price;
}
