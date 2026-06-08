package com.singeasy.booking_service.dto.req;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShopReqDto {
    @NotBlank(message = "Please fill in the shop name")
    private String name;

    @NotBlank(message = "Please fill in the city")
    private String city;

    @NotBlank(message = "Please fill in the phone number")
    private String phoneNumber;

    @NotBlank(message = "Please fill in the address")
    private String address;

    @NotBlank(message = "Please fill in the opening hours")
    private String openingHours;

    @Min(value = 0, message = "Please fill in the minimum price per hour")
    private Double minPricePerHour;

    @NotBlank(message = "Please upload an image for the shop")
    private String imageUrl;
    private String description;
    
    private List<String> amenities;
    private List<String> labels;
}
