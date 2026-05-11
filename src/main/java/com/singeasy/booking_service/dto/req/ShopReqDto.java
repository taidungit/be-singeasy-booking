package com.singeasy.booking_service.dto.req;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShopReqDto {
    @NotBlank(message = "Tên quán không được để trống")
    private String name;

    @NotBlank(message = "Thành phố không được để trống")
    private String city;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    private String openingHours;
    
    @Min(value = 0, message = "Giá không được nhỏ hơn 0")
    private Double minPricePerHour;

    private String imageUrl;
    private String description;
    
    private List<String> amenities;
    private List<String> labels;
}
