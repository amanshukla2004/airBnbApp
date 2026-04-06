package com.aman.project.airBnbApp.strategy;

import com.aman.project.airBnbApp.entity.Inventory;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

public interface PricingStrategy {
	BigDecimal calculatePrice(Inventory inventory);
}
