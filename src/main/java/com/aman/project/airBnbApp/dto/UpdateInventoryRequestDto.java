package com.aman.project.airBnbApp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class UpdateInventoryRequestDto {

	private LocalDate startDate;
	private LocalDate endDate;
	private BigDecimal surgeFactor;
	private Boolean closed;
}
