package com.singeasy.booking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OccupiedSlotDto {
    private String startTime; // Ví dụ: "16:00"
    private String endTime;   // Ví dụ: "18:00"
}