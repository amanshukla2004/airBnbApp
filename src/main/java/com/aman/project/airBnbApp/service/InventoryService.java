package com.aman.project.airBnbApp.service;

import com.aman.project.airBnbApp.entity.Room;

public interface InventoryService {
	void initialiseRoomForAYear(Room room);

	void deleteFutureInventories(Room room);
}
