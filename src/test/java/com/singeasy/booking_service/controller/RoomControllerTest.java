package com.singeasy.booking_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.singeasy.booking_service.dto.req.RoomReqDto;
import com.singeasy.booking_service.dto.res.RoomResDto;
import com.singeasy.booking_service.enums.RoomStatus;
import com.singeasy.booking_service.service.RoomService;

@WebMvcTest(RoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomService roomService;

    @Test
    void createRoom_returnsCreated() throws Exception {
        RoomReqDto request = validRoomRequest();
        RoomResDto response = new RoomResDto();
        response.setId(1L);
        response.setName("VIP 1");
        when(roomService.addRoomToShop(eq(1L), any(RoomReqDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/shops/1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("VIP 1"));
    }

    @Test
    void getRoomById_returnsOk() throws Exception {
        RoomResDto response = new RoomResDto();
        response.setId(10L);
        response.setName("Room 10");
        when(roomService.getRoomById(10L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/shops/1/rooms/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Room 10"));
    }

    @Test
    void updateRoom_returnsOk() throws Exception {
        RoomReqDto request = validRoomRequest();
        RoomResDto response = new RoomResDto();
        response.setId(10L);
        response.setName("Updated Room");
        when(roomService.updateRoom(eq(10L), any(RoomReqDto.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/shops/1/rooms/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Room"));
    }

    @Test
    void getRoomsByShop_returnsList() throws Exception {
        RoomResDto room = new RoomResDto();
        room.setId(1L);
        room.setName("Room A");
        when(roomService.getRoomsByShop(1L)).thenReturn(List.of(room));

        mockMvc.perform(get("/api/v1/shops/1/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Room A"));
    }

    @Test
    void deleteRoom_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/shops/1/rooms/10"))
                .andExpect(status().isNoContent());

        verify(roomService).deleteRoom(10L);
    }

    private RoomReqDto validRoomRequest() {
        RoomReqDto dto = new RoomReqDto();
        dto.setName("VIP 1");
        dto.setCapacity("10");
        dto.setPricePerHour(200.0);
        dto.setStatus(RoomStatus.AVAILABLE);
        dto.setImageUrl("http://image.url/room.png");
        return dto;
    }
}
