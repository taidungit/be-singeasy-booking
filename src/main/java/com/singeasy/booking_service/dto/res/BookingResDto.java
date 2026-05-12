package com.singeasy.booking_service.dto.res;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

import com.singeasy.booking_service.enums.BookingStatusEnum;

@Data
public class BookingResDto {
    private Long id;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private Integer duration;
    private Double pricePerHour;
    private Double serviceFee;
    private Double totalAmount;
    private BookingStatusEnum status; // PENDING, CONFIRMED, CANCELLED

    // Basic Room info to display on the history card
    private Long roomId;
    private String roomName; 
    
    // Basic User info
    private String userName;
    private String userEmail;
}
