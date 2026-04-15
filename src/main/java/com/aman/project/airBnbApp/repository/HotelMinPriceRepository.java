package com.aman.project.airBnbApp.repository;

import com.aman.project.airBnbApp.dto.HotelPriceDto;
import com.aman.project.airBnbApp.entity.Hotel;
import com.aman.project.airBnbApp.entity.HotelMinPrice;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelMinPriceRepository extends JpaRepository<HotelMinPrice, Long> {
	@Query(
		"""
                SELECT new com.aman.project.airBnbApp.dto.HotelPriceDto(i.hotel, AVG(i.price))
                FROM HotelMinPrice i
                WHERE LOWER(i.hotel.city) = LOWER(:city)
                    AND i.date BETWEEN :startDate AND :endDate
                    AND i.hotel.active = true
                GROUP BY i.hotel
                """
	)
	Page<HotelPriceDto> findHotelWithAvailableInventory(
		@Param("city") String city,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate,
		@Param("roomsCount") Integer roomsCount,
		@Param("dateCount") Long dateCount,
		Pageable pageable
	);

	Optional<HotelMinPrice> findByHotelAndDate(Hotel hotel, LocalDate date);

	void deleteByHotel(Hotel hotel);
}
