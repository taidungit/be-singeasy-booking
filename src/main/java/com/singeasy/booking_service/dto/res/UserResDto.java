package com.singeasy.booking_service.dto.res;

import com.singeasy.booking_service.enums.RoleEnum;

import lombok.Data;

@Data
public class UserResDto {
    private Long id;
    private String name;
    private String email;
    private String avatar;
    private String phoneNumber;
    private RoleEnum role;
}