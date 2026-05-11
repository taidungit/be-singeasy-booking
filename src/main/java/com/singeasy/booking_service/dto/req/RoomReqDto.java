package com.singeasy.booking_service.dto.req;

import java.util.List;

import com.singeasy.booking_service.enums.RoomStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomReqDto {
    @NotBlank(message = "Tên phòng không được để trống")
    private String name;

    @NotBlank(message = "Sức chứa không được để trống")
    private String capacity;

    @NotNull(message = "Giá mỗi giờ không được để trống")
    @Min(value = 0, message = "Giá mỗi giờ phải lớn hơn hoặc bằng 0")
    private Double pricePerHour;

    @NotNull(message = "Trạng thái phòng không được để trống")
    private RoomStatus status;

    @NotBlank(message = "Ảnh phòng không được để trống")
    private String imageUrl;

    private List<String> amenities;
}
