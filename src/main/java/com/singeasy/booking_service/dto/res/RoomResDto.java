package com.singeasy.booking_service.dto.res;

import java.util.List;

import com.singeasy.booking_service.enums.RoomStatus;

import lombok.Data;

@Data
public class RoomResDto {
private Long id; 
    private String name;
    private String capacity;
    private Double pricePerHour;
    private RoomStatus status;
    private String imageUrl;
    private List<String> amenities;
    private Long shopId; 
    private String shopName; 
}