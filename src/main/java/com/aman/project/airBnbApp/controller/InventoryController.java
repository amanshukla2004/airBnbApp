package com.aman.project.airBnbApp.controller;

import com.aman.project.airBnbApp.dto.InventoryDto;
import com.aman.project.airBnbApp.dto.UpdateInventoryRequestDto;
import com.aman.project.airBnbApp.service.InventoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/inventory")
@RequiredArgsConstructor
public class InventoryController {

	private final InventoryService inventoryService;

	@GetMapping("/rooms/{roomId}")
	public ResponseEntity<List<InventoryDto>> getAllInventoryByRoomId(@PathVariable("roomId") Long roomId) {
		return ResponseEntity.ok(inventoryService.getAllInventoryByRoom(roomId));
	}

	@PatchMapping("/rooms/{roomId}")
	public ResponseEntity<Void> updateInventory(
		@PathVariable("roomId") Long roomId,
		@RequestBody UpdateInventoryRequestDto updateInventoryRequestDto
	) {
		//
		inventoryService.updateInventory(roomId, updateInventoryRequestDto);
		return ResponseEntity.noContent().build();
	}
}
