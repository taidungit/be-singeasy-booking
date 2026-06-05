package com.singeasy.booking_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.singeasy.booking_service.dto.req.UpdateProfileReqDto;
import com.singeasy.booking_service.dto.req.UserReqDto;
import com.singeasy.booking_service.dto.res.UserResDto;
import com.singeasy.booking_service.enums.RoleEnum;
import com.singeasy.booking_service.service.UserService;
import com.singeasy.booking_service.util.SecurityUtil;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void getAllUsers_returnsOk() throws Exception {
        UserResDto user = new UserResDto();
        user.setId(1L);
        user.setEmail("user@test.com");
        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("user@test.com"));
    }

    @Test
    void getUserById_returnsOk() throws Exception {
        UserResDto user = new UserResDto();
        user.setId(1L);
        user.setName("Test User");
        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void createUser_returnsOk() throws Exception {
        UserReqDto request = new UserReqDto();
        request.setName("New User");
        request.setEmail("new@test.com");

        UserResDto response = new UserResDto();
        response.setId(2L);
        response.setEmail("new@test.com");
        when(userService.createUser(any(UserReqDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@test.com"));
    }

    @Test
    void updateUser_returnsOk() throws Exception {
        UserReqDto request = new UserReqDto();
        request.setName("Updated");
        request.setEmail("updated@test.com");

        UserResDto response = new UserResDto();
        response.setName("Updated");
        when(userService.updateUser(eq(1L), any(UserReqDto.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void deleteUser_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    void updateProfile_returnsOk() throws Exception {
        UpdateProfileReqDto request = new UpdateProfileReqDto();
        request.setName("Profile Name");
        request.setPhoneNumber("0900000000");

        UserResDto response = new UserResDto();
        response.setEmail("user@test.com");
        response.setName("Profile Name");
        response.setRole(RoleEnum.USER);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserLogin).thenReturn(Optional.of("user@test.com"));
            when(userService.updateProfile(eq("user@test.com"), any(UpdateProfileReqDto.class)))
                    .thenReturn(response);

            mockMvc.perform(put("/api/v1/users/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Profile Name"));
        }
    }
}
