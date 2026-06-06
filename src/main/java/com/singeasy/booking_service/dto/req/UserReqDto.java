package com.singeasy.booking_service.dto.req;

import com.singeasy.booking_service.enums.RoleEnum;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserReqDto {
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    private String password;
    private String avatar;
    private String phoneNumber;
    private RoleEnum role;
}