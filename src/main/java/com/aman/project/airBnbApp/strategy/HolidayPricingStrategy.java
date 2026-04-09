package com.aman.project.airBnbApp.strategy;

import com.aman.project.airBnbApp.entity.Inventory;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
public class HolidayPricingStrategy implements PricingStrategy {

	private final PricingStrategy wrapped;

	@Override
	public BigDecimal calculatePrice(Inventory inventory) {
		BigDecimal price = wrapped.calculatePrice(inventory);

		boolean isTodayHoliday = true;
		// call an API or check with local data like array
		if (isTodayHoliday) {
			price = price.multiply(BigDecimal.valueOf(1.25));
		}
		return price;
	}
}
