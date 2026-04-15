package com.aman.project.airBnbApp.repository;

import com.aman.project.airBnbApp.entity.Booking;
import com.aman.project.airBnbApp.entity.Hotel;
import com.aman.project.airBnbApp.entity.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {
	Optional<Booking> findByPaymentSessionId(String sessionId);

	List<Booking> findByHotel(Hotel hotel);

	List<Booking> findByHotelAndCreatedAtBetween(Hotel hotel, LocalDateTime startDate, LocalDateTime endDate);

	List<Booking> findByUser(User user);

	@Modifying
	@Query(value = "DELETE FROM booking_guest WHERE guest_id = :guestId", nativeQuery = true)
	void deleteGuestReferences(@Param("guestId") Long guestId);

	void deleteByHotel(Hotel hotel);
}
