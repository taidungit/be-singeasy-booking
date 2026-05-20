package com.singeasy.booking_service.controller;

import com.singeasy.booking_service.dto.res.DashboardSummaryRes;
import com.singeasy.booking_service.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryRes> getSummary() {
        return ResponseEntity.ok(dashboardService.getDashboardSummary());
    }
}