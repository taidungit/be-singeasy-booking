package com.singeasy.booking_service.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewReqDto {

    @NotNull(message = "Rating cannot be null")
    @Min(value = 1, message = "Rating must be at least 1 star")
    @Max(value = 5, message = "Rating cannot exceed 5 stars")
    private Integer rating; 

    @NotBlank(message = "Comment cannot be empty")
    private String comment; 

    @NotNull(message = "Shop ID cannot be null")
    private Long shopId;
}