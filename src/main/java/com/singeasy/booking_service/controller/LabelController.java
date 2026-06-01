package com.singeasy.booking_service.controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.singeasy.booking_service.entity.Label;
import com.singeasy.booking_service.repository.LabelRepository;

@RestController
@RequestMapping("/api/v1/")
public class LabelController {
    private final LabelRepository labelRepository;
    public LabelController(LabelRepository labelRepository) {
        this.labelRepository = labelRepository;
    }
    @GetMapping("/labels")
    public ResponseEntity<List<String>> getAllLabelNames() {
        List<String> names = labelRepository.findAll()
                .stream()
                .map(Label::getName)
                .toList();
        return ResponseEntity.ok(names);
    }
}