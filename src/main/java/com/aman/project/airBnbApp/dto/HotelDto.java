package com.aman.project.airBnbApp.dto;

import com.aman.project.airBnbApp.entity.HotelContactInfo;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
public class HotelDto {

	private long id;

	private String name;

	private String city;

	private String[] photos;

	private String[] amenities;

	private HotelContactInfo contactInfo;

	private Boolean active;
}
