package com.singeasy.booking_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.singeasy.booking_service.service.ShopService;
import com.singeasy.booking_service.service.RoomService;
import java.util.List;

@RestController
@RequestMapping("/api/v1/search-utilities")
@RequiredArgsConstructor
public class SearchUtilityController {

    private final ShopService shopService;
    private final RoomService roomService;

    @GetMapping("/cities")
    public ResponseEntity<List<String>> getActiveCities() {
        List<String> cities = shopService.getDistinctCities();
        return ResponseEntity.ok(cities);
    }

    @GetMapping("/capacities")
    public ResponseEntity<List<String>> getRoomCapacities() {
        List<String> capacities = roomService.getDistinctRoomCapacities();
        return ResponseEntity.ok(capacities);
    }
}