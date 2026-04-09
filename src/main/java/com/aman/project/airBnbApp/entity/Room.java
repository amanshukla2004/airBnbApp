package com.aman.project.airBnbApp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter
@Setter
@Table
public class Room {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// owner side
	// when you create a room you have to specify the hotel

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "hotel_id", nullable = false)
	//@JsonIgnore
	private Hotel hotel;

	@Column(nullable = false)
	private String type;

	@Column(nullable = false, precision = 10, scale = 2) // rs 234.45 not 234.567
	private BigDecimal basePrice;

	@Column(columnDefinition = "TEXT[]") // read this and add it to the notes
	private String[] photos;

	@Column(columnDefinition = "TEXT[]") // {"WIFI", "SWIMMING POOL", "..."}
	private String[] amenities;

	@Column(nullable = false)
	private Integer totalCount;

	@Column(nullable = false)
	private Integer capacity;

	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;
}
