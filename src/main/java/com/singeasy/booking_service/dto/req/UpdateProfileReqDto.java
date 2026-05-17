package com.singeasy.booking_service.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileReqDto {
    @NotBlank(message = "Name cannot be blank")
    private String name;
    
    private String phoneNumber;
    
    // Nhận chuỗi Base64 siêu dài từ Frontend gửi lên
    private String avatar; 
}