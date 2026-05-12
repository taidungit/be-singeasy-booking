package com.singeasy.booking_service.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import com.singeasy.booking_service.enums.BookingStatusEnum;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
@Entity
@Getter
@Setter
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate bookingDate; 
    private LocalTime startTime;   
    private Integer duration;      
    private Double pricePerHour;   
    private Double serviceFee;     
    private Double totalAmount;    // = pricePerHour * duration + serviceFee
    @Enumerated(EnumType.STRING)
    private BookingStatusEnum status;// PENDING, CONFIRMED, CANCELLED

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room; 
}
