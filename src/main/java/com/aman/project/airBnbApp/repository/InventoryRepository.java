package com.aman.project.airBnbApp.repository;

import com.aman.project.airBnbApp.entity.Inventory;
import com.aman.project.airBnbApp.entity.Room;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
	void deleteByDateAfterAndRoom(LocalDate date, Room room);
}
