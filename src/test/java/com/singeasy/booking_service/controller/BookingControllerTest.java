package com.singeasy.booking_service.controller;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
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
import com.singeasy.booking_service.dto.req.BookingReqDto;
import com.singeasy.booking_service.dto.res.BookingResDto;
import com.singeasy.booking_service.entity.Booking;
import com.singeasy.booking_service.entity.User;
import com.singeasy.booking_service.enums.BookingStatusEnum;
import com.singeasy.booking_service.repository.BookingRepository;
import com.singeasy.booking_service.service.BookingService;
import com.singeasy.booking_service.service.UserService;
import com.singeasy.booking_service.util.SecurityUtil;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private UserService userService;

    @MockBean
    private BookingRepository bookingRepository;

    @Test
    void createBooking_returnsOk() throws Exception {
        BookingReqDto request = new BookingReqDto();
        request.setRoomId(1L);
        request.setBookingDate(LocalDate.of(2026, 6, 5));
        request.setStartTime(LocalTime.of(18, 0));
        request.setDuration(2);

        BookingResDto response = new BookingResDto();
        response.setId(1L);
        response.setStatus(BookingStatusEnum.PENDING);
        when(bookingService.createBooking(request)).thenReturn(response);

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void cancelBooking_returnsOk() throws Exception {
        BookingResDto response = new BookingResDto();
        response.setId(1L);
        response.setStatus(BookingStatusEnum.CANCELLED);
        when(bookingService.cancelBooking(1L)).thenReturn(response);

        mockMvc.perform(put("/api/v1/bookings/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void getHistory_withoutLogin_returnsUnauthorized() throws Exception {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserLogin).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/bookings/history"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void getHistory_withLogin_returnsHistory() throws Exception {
        User user = new User();
        user.setId(5L);
        user.setEmail("user@test.com");

        BookingResDto booking = new BookingResDto();
        booking.setId(1L);
        booking.setUserEmail("user@test.com");

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserLogin).thenReturn(Optional.of("user@test.com"));
            when(userService.getUserByEmail("user@test.com")).thenReturn(user);
            when(bookingService.getUserHistory(5L)).thenReturn(List.of(booking));

            mockMvc.perform(get("/api/v1/bookings/history"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].userEmail").value("user@test.com"));
        }
    }

    @Test
    void getBookingsByShop_returnsOk() throws Exception {
        BookingResDto booking = new BookingResDto();
        booking.setShopId(1L);
        when(bookingService.getBookingsByShopId(1L)).thenReturn(List.of(booking));

        mockMvc.perform(get("/api/v1/bookings/shop/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shopId").value(1));
    }

    @Test
    void getAllBookings_returnsOk() throws Exception {
        when(bookingService.getAllBookings()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void approveBooking_returnsOk() throws Exception {
        BookingResDto response = new BookingResDto();
        response.setId(1L);
        response.setStatus(BookingStatusEnum.CONFIRMED);
        when(bookingService.approveBooking(1L)).thenReturn(response);

        mockMvc.perform(put("/api/v1/bookings/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void getOccupiedSlots_returnsFormattedSlots() throws Exception {
        Booking booking = new Booking();
        booking.setBookingDate(LocalDate.of(2026, 6, 5));
        booking.setStartTime(LocalTime.of(22, 0));
        booking.setDuration(3);

        when(bookingRepository.findActiveBookingsByRoomAndDate(1L, LocalDate.of(2026, 6, 5)))
                .thenReturn(List.of(booking));

        mockMvc.perform(get("/api/v1/bookings/occupied-slots")
                        .param("roomId", "1")
                        .param("date", "2026-06-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].startTime").value("22:00"))
                .andExpect(jsonPath("$[0].endTime").value("24:00"));

        verify(bookingRepository).findActiveBookingsByRoomAndDate(1L, LocalDate.of(2026, 6, 5));
    }
}
