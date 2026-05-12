package com.singeasy.booking_service.dto.req;

import com.singeasy.booking_service.enums.RoleEnum;
import lombok.Data;

@Data
public class UserReqDto {
    private String name;
    private String email;
    private String password;
    private String avatar;
    private String phoneNumber;
    private RoleEnum role;
}