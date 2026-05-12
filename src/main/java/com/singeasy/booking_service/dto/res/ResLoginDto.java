package com.singeasy.booking_service.dto.res;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.singeasy.booking_service.enums.RoleEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
public class ResLoginDto {
    @JsonProperty("access_token")
    private String accessToken;
    private String refreshToken;
    private UserLogin user;
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserLogin {
        private Long id;
        private String email;
        private String name;
        private RoleEnum role;
    }
    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserGetAccount{
        private UserLogin user;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInsideToken{
        private Long id;
        private String email;
        private String name;
    }

}

