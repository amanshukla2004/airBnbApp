package com.aman.project.airBnbApp.service;

import com.aman.project.airBnbApp.entity.Hotel;
import com.aman.project.airBnbApp.entity.HotelMinPrice;
import com.aman.project.airBnbApp.entity.Inventory;
import com.aman.project.airBnbApp.repository.HotelMinPriceRepository;
import com.aman.project.airBnbApp.repository.HotelRepository;
import com.aman.project.airBnbApp.repository.InventoryRepository;
import com.aman.project.airBnbApp.strategy.PricingService;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PricingUpdateService {

	// scheduler to update the inventory and HotelMinPrice tables every hour
	private final HotelRepository hotelRepository;
	private final InventoryRepository inventoryRepository;
	private final HotelMinPriceRepository hotelMinPriceRepository;
	private final PricingService pricingService;

	@Transactional
	//@Scheduled(cron = "*/5 * * * * *") // every 5 sec

	@Scheduled(cron = "0 0 * * * *") // cron expression
	public void updatePrice() {
		int page = 0;
		int batchSize = 100;
		while (true) {
			Page<Hotel> hotelPage = hotelRepository.findAll(PageRequest.of(page, batchSize));
			if (hotelPage.isEmpty()) {
				break;
			}
			hotelPage.getContent().forEach(this::updateHotelPrice);

			page++;
		}
	}

	private void updateHotelPrice(Hotel hotel) {
		//
		log.info("Updating hotel price for hotel ID: {}", hotel.getId());
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = startDate.plusYears(1);

		List<Inventory> inventoryList = inventoryRepository.findByHotelAndDateBetween(hotel, startDate, endDate);
		updateInventoryPrices(inventoryList);
		updateHotelMinPrice(hotel, inventoryList, startDate, endDate);
	}

	private void updateHotelMinPrice(
		Hotel hotel,
		List<Inventory> inventoryList,
		LocalDate startDate,
		LocalDate endDate
	) {
		// Compute minimum price per pay for the hotel
		Map<LocalDate, BigDecimal> dailyMinPrices = inventoryList
			.stream()
			.collect(
				Collectors.groupingBy(
					Inventory::getDate,
					Collectors.mapping(Inventory::getPrice, Collectors.minBy(Comparator.naturalOrder()))
				)
			)
			.entrySet()
			.stream()
			.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().orElse(BigDecimal.ZERO)));

		// Prepare HotelPrice Entities in bulk
		List<HotelMinPrice> hotelPrices = new ArrayList<>();
		dailyMinPrices.forEach((date, price) -> {
			HotelMinPrice hotelPrice = hotelMinPriceRepository
				.findByHotelAndDate(hotel, date)
				.orElse(new HotelMinPrice(hotel, date));
			hotelPrice.setPrice(price);
			hotelPrices.add(hotelPrice);
		});
		// save all HotelPrice entities in bulk
		hotelMinPriceRepository.saveAll(hotelPrices);
	}

	private void updateInventoryPrices(List<Inventory> inventoryList) {
		inventoryList.forEach(inventory -> {
			BigDecimal dynamicPrice = pricingService.calculateDynamicPricing(inventory);
			inventory.setPrice(dynamicPrice);
		});
		inventoryRepository.saveAll(inventoryList);
	}
}
