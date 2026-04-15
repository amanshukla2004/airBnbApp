package com.aman.project.airBnbApp.repository;

import com.aman.project.airBnbApp.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aman.project.airBnbApp.entity.User;
import java.util.List;

public interface GuestRepository extends JpaRepository<Guest, Long> {
    List<Guest> findByUser(User user);
}
