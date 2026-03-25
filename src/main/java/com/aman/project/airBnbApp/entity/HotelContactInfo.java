package com.aman.project.airBnbApp.entity;


import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable // ? address, phoneNumber will be taken from here and put inside the Hotel entity


public class HotelContactInfo {

    private String address;
    private String phoneNumber;
    private String email;
    private String location;// longitute and latitude
}
