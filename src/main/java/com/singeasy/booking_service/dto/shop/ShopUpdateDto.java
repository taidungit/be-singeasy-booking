package com.singeasy.booking_service.dto.shop;

import java.util.List;

import lombok.Data;

@Data
public class ShopUpdateDto {
    private String name;
    private String city;
    private String phoneNumber;
    private String address;
    private String openingHours;
    private Double minPricePerHour;
    private String imageUrl;
    private String description;
    private List<String> amenities;
    private List<String> labels;
}
