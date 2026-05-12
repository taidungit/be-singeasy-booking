package com.singeasy.booking_service.controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.singeasy.booking_service.entity.Amenity;
import com.singeasy.booking_service.repository.AmenityRepository;

@RestController
@RequestMapping("/api/v1/")
public class AmenityController {
    private final AmenityRepository amenityRepository;
    public AmenityController(AmenityRepository amenityRepository) {
        this.amenityRepository = amenityRepository;
    }
    @GetMapping("/amenities")
    public ResponseEntity<List<String>> getAllAmenityNames() {
        List<String> names = amenityRepository.findAll()
                .stream()
                .map(Amenity::getName)
                .toList();
        return ResponseEntity.ok(names);
    }
}