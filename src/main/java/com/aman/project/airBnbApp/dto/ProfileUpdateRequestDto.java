package com.aman.project.airBnbApp.dto;

import com.aman.project.airBnbApp.entity.enums.Gender;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ProfileUpdateRequestDto {

	private String name;
	private LocalDate dateOfBirth;
	private Gender gender;
}
