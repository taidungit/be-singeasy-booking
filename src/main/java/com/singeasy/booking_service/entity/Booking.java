package com.singeasy.booking_service.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.singeasy.booking_service.enums.BookingStatusEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bookings")
@Getter
@Setter
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate bookingDate; 

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;   
    @Column(name = "duration_hours")
    private Integer duration;      

    private Double pricePerHour;   
    private Double serviceFee = 0.0;     
    private Double totalAmount;    

    private Long shopId;
    private String shopName;
    private String roomName;

    @Enumerated(EnumType.STRING)
    private BookingStatusEnum status = BookingStatusEnum.PENDING; 

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room; 
}