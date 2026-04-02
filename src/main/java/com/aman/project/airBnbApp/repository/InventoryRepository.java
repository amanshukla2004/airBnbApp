package com.aman.project.airBnbApp.repository;

import com.aman.project.airBnbApp.dto.HotelSearchRequest;
import com.aman.project.airBnbApp.entity.Hotel;
import com.aman.project.airBnbApp.entity.Inventory;
import com.aman.project.airBnbApp.entity.Room;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
	void deleteByRoom(Room room);

	@Query(
		"""
			SELECT DISTINCT i.hotel
			FROM Inventory i
			WHERE i.city = :city
				AND i.date BETWEEN :startDate AND :endDate
				AND i.closed = false
				AND (i.totalCount - i.bookedCount - i.reservedCount) >= :roomsCount
			GROUP BY i.hotel, i.room
			Having COUNT(i.date) = :dateCount
			"""
	)
	Page<Hotel> findHotelWithAvailableInventory(
		@Param("city") String city,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate,
		@Param("roomsCount") Integer roomsCount,
		@Param("dateCount") Long dateCount,
		Pageable pageable
	);

	@Query(
		"""
		SELECT i
		FROM Inventory i
		WHERE i.room.id = :roomId
			AND i.date BETWEEN :startDate AND :endDate
				AND i.closed = false
				AND (i.totalCount - i.bookedCount - i.reservedCount) >= :roomsCount
		
"""
	)
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	List<Inventory> findAndLockAvailableInventory(
		@Param("roomId") Long roomId,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate,
		@Param("roomsCount") Integer roomsCount
	);
}
