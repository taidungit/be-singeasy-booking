package com.singeasy.booking_service.dto.req;

import jakarta.validation.constraints.NotBlank;

public class LoginDto {
    @NotBlank(message="Dont't have username")
    private String username;
    @NotBlank(message ="Don't have password")
    private String password;
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    @Override
    public String toString() {
        return "LoginDTO [username=" + username + ", password=" + password + "]";
    }
    
}
