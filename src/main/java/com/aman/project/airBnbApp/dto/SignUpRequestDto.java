package com.aman.project.airBnbApp.dto;

import lombok.Data;

@Data
public class SignUpRequestDto {

	private String email;
	private String password;
	private String name;
}
//TODO: add validation in SignUpRequestDto
