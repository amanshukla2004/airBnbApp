package com.aman.project.airBnbApp.strategy;

import com.aman.project.airBnbApp.entity.Inventory;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

	//          DECORATOR DESIGN PATTERN
	public BigDecimal calculateDynamicPricing(Inventory inventory) {
		PricingStrategy pricingStrategy = new BasePricingStrategy();
		// apply the additional strategy

		pricingStrategy = new SurgePricingStrategy(pricingStrategy);
		pricingStrategy = new OccupancyPricingStrategy(pricingStrategy);
		pricingStrategy = new UrgencyPricingStrategy(pricingStrategy);
		pricingStrategy = new HolidayPricingStrategy(pricingStrategy);

		return pricingStrategy.calculatePrice(inventory);
	}
}
