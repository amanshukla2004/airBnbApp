package com.aman.project.airBnbApp.repository;

import com.aman.project.airBnbApp.entity.Booking;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
	Optional<Booking> findByPaymentSessionId(String sessionId);
}
