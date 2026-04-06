package com.aman.project.airBnbApp.strategy;

import com.aman.project.airBnbApp.entity.Inventory;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SurgePricingStrategy implements PricingStrategy {

	private final PricingStrategy wrapped;

	@Override
	public BigDecimal calculatePrice(Inventory inventory) {
		BigDecimal price = wrapped.calculatePrice(inventory);
		return price.multiply(inventory.getSurgeFactor());
	}
}
