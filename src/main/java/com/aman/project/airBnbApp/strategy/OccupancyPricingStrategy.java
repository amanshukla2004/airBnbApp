package com.aman.project.airBnbApp.strategy;

import com.aman.project.airBnbApp.entity.Inventory;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OccupancyPricingStrategy implements PricingStrategy {

	private final PricingStrategy wrapped;

	@Override
	public BigDecimal calculatePrice(Inventory inventory) {
		BigDecimal price = wrapped.calculatePrice(inventory);
		double occupancyRate = (double) inventory.getBookedCount() / inventory.getTotalCount();
		if (occupancyRate > 0.8) {
			price = price.multiply(BigDecimal.valueOf(1.2));
		}
		return price;
	}
}
