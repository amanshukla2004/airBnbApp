package com.aman.project.airBnbApp.repository;

import com.aman.project.airBnbApp.dto.HotelSearchRequest;
import com.aman.project.airBnbApp.entity.Hotel;
import com.aman.project.airBnbApp.entity.Inventory;
import com.aman.project.airBnbApp.entity.Room;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
	void deleteByRoom(Room room);
 
	boolean existsByRoomAndDate(Room room, LocalDate date);

	@Query(
		"""
			SELECT DISTINCT i.hotel
			FROM Inventory i
			WHERE i.hotel.active = true
				AND LOWER(i.city) = LOWER(:city)
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

	//-----------------------------
	@Query(
		"""
            SELECT i
            FROM Inventory i
            WHERE i.room.id = :roomId
                AND i.date BETWEEN :startDate AND :endDate
                AND (i.totalCount - i.bookedCount) >= :numberOfRooms
                AND i.closed = false
    
    
    """
	)
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	List<Inventory> findAndLockReservedInventory(
		@Param("roomId") Long roomId,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate,
		@Param("numberOfRooms") int numberOfRooms
	);

	/// ///////////////////////////////////////////////////////
	@Modifying // Question
	@Query(
		"""
            UPDATE Inventory i
            SET i.reservedCount = i.reservedCount + :numberOfRooms
            WHERE i.room.id = :roomId
                AND i.date BETWEEN :startDate AND :endDate
                AND (i.totalCount - i.bookedCount -i.reservedCount) >= :numberOfRooms
                AND i.closed = false
    """
	)
	void initBooking(
		@Param("roomId") Long roomId,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate,
		@Param("numberOfRooms") int numberOfRooms
	);

	/// ////////////////////////////////////////////
	@Modifying // Question
	@Query(
		"""
		UPDATE Inventory i
		SET i.reservedCount = i.reservedCount - :numberOfRooms,
			i.bookedCount = i.bookedCount + :numberOfRooms
		WHERE i.room.id = :roomId
			AND i.date BETWEEN :startDate AND :endDate
			AND (i.totalCount - i.bookedCount) >= :numberOfRooms
			AND i.reservedCount >= :numberOfRooms
			AND i.closed = false


"""
	)
	void confirmBooking(
		@Param("roomId") Long roomId,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate,
		@Param("numberOfRooms") int numberOfRooms
	);

	/// For cancel booking
	///
	///
	@Modifying // Question
	@Query(
		"""
            UPDATE Inventory i
            SET i.bookedCount = i.bookedCount - :numberOfRooms
            WHERE i.room.id = :roomId
                AND i.date BETWEEN :startDate AND :endDate
                AND (i.totalCount - i.bookedCount) >= :numberOfRooms
                AND i.closed = false
    
    
    """
	)
	void cancelBooking(
		@Param("roomId") Long roomId,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate,
		@Param("numberOfRooms") int numberOfRooms
	);

	List<Inventory> findByHotelAndDateBetween(Hotel hotel, LocalDate startDate, LocalDate endDate);

	List<Inventory> findByRoomOrderByDate(Room room);

	/// ///
	///
	@Query(
		"""
                    SELECT  i
                      FROM Inventory i
                                
                    WHERE i.room.id = :roomId
                        AND i.date BETWEEN :startDate AND :endDate
                        
            
            
            """
	)
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	List<Inventory> getInventoryAndLockBeforeUpdate(
		@Param("roomId") Long roomId,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate
	);

	@Modifying
	@Query(
		"""
                UPDATE Inventory i
                SET i.surgeFactor = COALESCE(:surgeFactor, i.surgeFactor),
                        i.closed = COALESCE(:closed, i.closed),
                        i.price = COALESCE(:price, i.price)
                WHERE i.room.id = :roomId
                    AND i.date BETWEEN :startDate AND :endDate
        """
	)
	void updateInventory(
		@Param("roomId") Long roomId,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate,
		@Param("closed") Boolean closed,
		@Param("surgeFactor") BigDecimal surgeFactor,
		@Param("price") BigDecimal price
	);

	@Modifying
	@Query(
		"""
                UPDATE Inventory i
                SET i.price = :price
                WHERE i.room.id = :roomId
                    AND i.date >= :startDate
                    AND i.bookedCount = 0
        """
	)
	void updateBasePriceForFutureOpenInventories(
		@Param("roomId") Long roomId,
		@Param("price") BigDecimal price,
		@Param("startDate") LocalDate startDate
	);
}
