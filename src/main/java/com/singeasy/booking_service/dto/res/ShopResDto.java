package com.singeasy.booking_service.dto.res;

import java.util.List;
import com.singeasy.booking_service.enums.ShopStatus;
import lombok.Data;

@Data
public class ShopResDto {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String phoneNumber;
    private String openingHours;
    private String description;
    private Double rating;
    private Integer reviewCount;
    private Double minPricePerHour;
    private String imageUrl;
    private ShopStatus status;

    private List<String> amenities;
    private List<String> labels;
    private List<ReviewResDto> reviews;
    
}