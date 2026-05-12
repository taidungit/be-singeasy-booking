package com.singeasy.booking_service.dto.req;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BookingReqDto {
    private Long roomId;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private Integer duration;
    private Double serviceFee;
    
    // Note: PricePerHour will be taken directly from the Room entity 
    // in the Backend to ensure data integrity.
}