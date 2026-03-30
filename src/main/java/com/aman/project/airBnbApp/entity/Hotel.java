package com.aman.project.airBnbApp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter
@Setter
@Table(name = "hotel")
public class Hotel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(nullable = false)
	private String name;

	private String city;

	@Column(columnDefinition = "TEXT[]") // read this and add it to the notes
	private String[] photos;

	@Column(columnDefinition = "TEXT[]") // {"WIFI", "SWIMMING POOL", "..."}
	private String[] amenities;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;

	@Embedded // @Embeddable in HotelContactInfo all the fields from there wil come here
	private HotelContactInfo contactInfo;

	// contact_info_phone_number
	// contact_info_address

	@Column(nullable = false)
	private Boolean active; // hotel active or not

	//    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY)
	//    private List<Room> rooms;

	@ManyToOne
	private User owner;

	@OneToMany(mappedBy = "hotel")
	private List<Room> rooms;
}
