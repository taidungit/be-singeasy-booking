package com.singeasy.booking_service.dto.res;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReviewResDto {
    private Long id;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private String userName; 
    private String userEmail;
}
