package com.aman.project.airBnbApp.strategy;

import com.aman.project.airBnbApp.entity.Inventory;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.MonthDay;
import java.util.Set;

@RequiredArgsConstructor
public class HolidayPricingStrategy implements PricingStrategy {

	private final PricingStrategy wrapped;

	// Approximation of major holidays including floating Indian holidays for the active years (2025/2026).
	// In a production system, this could be driven by a database table or 3rd party Holiday API.
	private static final Set<MonthDay> MAJOR_HOLIDAYS = Set.of(
		MonthDay.of(1, 1),   // New Year's Day
		MonthDay.of(12, 25), // Christmas Day
		MonthDay.of(10, 31), // Diwali (Approx 2024/2025)
		MonthDay.of(11, 1),  // Diwali
		MonthDay.of(3, 14),  // Holi (Approx 2025)
		MonthDay.of(3, 25)   // Holi
	);

	/**
	 * Calculates price dynamically:
	 * Multiplies by 1.25x if the Inventory date lands on a configured major holiday.
	 *
	 * @param inventory The inventory date context
	 * @return The final surged price for a holiday.
	 */
	@Override
	public BigDecimal calculatePrice(Inventory inventory) {
		BigDecimal price = wrapped.calculatePrice(inventory);
		
		MonthDay inventoryDay = MonthDay.from(inventory.getDate());
		boolean isTodayHoliday = MAJOR_HOLIDAYS.contains(inventoryDay);

		if (isTodayHoliday) {
			price = price.multiply(BigDecimal.valueOf(1.25));
		}
		return price;
	}
}
