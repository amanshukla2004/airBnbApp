package com.aman.project.airBnbApp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.aman.project.airBnbApp.entity.enums.Gender;
import java.time.LocalDate;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileUpdateRequestDto {

	private String name;

	@JsonProperty("dob")
	private LocalDate dateOfBirth;

	private Gender gender;
}
