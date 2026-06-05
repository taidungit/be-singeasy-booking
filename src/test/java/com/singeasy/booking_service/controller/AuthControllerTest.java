package com.singeasy.booking_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.singeasy.booking_service.dto.req.LoginDto;
import com.singeasy.booking_service.dto.res.ResLoginDto;
import com.singeasy.booking_service.dto.res.UserResDto;
import com.singeasy.booking_service.entity.User;
import com.singeasy.booking_service.enums.RoleEnum;
import com.singeasy.booking_service.service.UserService;
import com.singeasy.booking_service.util.SecurityUtil;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "dungmount.jwt.refresh-token-validity-in-seconds=3600")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManagerBuilder authenticationManagerBuilder;

    @MockBean
    private SecurityUtil securityUtil;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private AuthenticationManager authenticationManager;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        authentication = mock(Authentication.class);
        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
    }

    @Test
    void login_returnsAccessTokenAndCookie() throws Exception {
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername("user@test.com");
        loginDto.setPassword("password");

        User user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setName("Test User");
        user.setRole(RoleEnum.USER);
        user.setAvatar("avatar-base64");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user@test.com");
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);
        when(securityUtil.createAcessToken(eq("user@test.com"), any(ResLoginDto.class))).thenReturn("access-token");
        when(securityUtil.createRefreshToken(eq("user@test.com"), any(ResLoginDto.class))).thenReturn("refresh-token");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("access-token"))
                .andExpect(jsonPath("$.user.email").value("user@test.com"))
                .andExpect(header().exists("Set-Cookie"));

        verify(userService).updateUserToken("refresh-token", "user@test.com");
    }

    @Test
    void getAccount_returnsCurrentUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setName("Test User");
        user.setRole(RoleEnum.USER);
        user.setAvatar("avatar-base64");

        try (MockedStatic<SecurityUtil> securityUtilStatic = mockStatic(SecurityUtil.class)) {
            securityUtilStatic.when(SecurityUtil::getCurrentUserLogin).thenReturn(Optional.of("user@test.com"));
            when(userService.getUserByEmail("user@test.com")).thenReturn(user);

            mockMvc.perform(get("/api/v1/auth/account"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.user.email").value("user@test.com"))
                    .andExpect(jsonPath("$.user.name").value("Test User"));
        }
    }

    @Test
    void register_returnsCreated() throws Exception {
        User request = new User();
        request.setName("New User");
        request.setEmail("new@test.com");
        request.setPassword("password123");

        User created = new User();
        created.setId(2L);
        created.setName("New User");
        created.setEmail("new@test.com");
        created.setRole(RoleEnum.USER);

        UserResDto response = new UserResDto();
        response.setId(2L);
        response.setEmail("new@test.com");
        response.setName("New User");

        when(userService.isEmailExist("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userService.createUser(any(User.class))).thenReturn(created);
        when(userService.convertToResDto(created)).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new@test.com"));
    }

    @Test
    void logOut_clearsRefreshToken() throws Exception {
        try (MockedStatic<SecurityUtil> securityUtilStatic = mockStatic(SecurityUtil.class)) {
            securityUtilStatic.when(SecurityUtil::getCurrentUserLogin).thenReturn(Optional.of("user@test.com"));

            mockMvc.perform(post("/api/v1/auth/logout"))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Set-Cookie"));

            verify(userService).updateUserToken(null, "user@test.com");
        }
    }
}
