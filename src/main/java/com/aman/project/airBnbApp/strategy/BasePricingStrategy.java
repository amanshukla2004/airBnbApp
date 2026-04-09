package com.aman.project.airBnbApp.strategy;

import com.aman.project.airBnbApp.entity.Inventory;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

public class BasePricingStrategy implements PricingStrategy {

	@Override
	public BigDecimal calculatePrice(Inventory inventory) {
		return inventory.getRoom().getBasePrice();
	}
}
