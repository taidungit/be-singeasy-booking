package com.singeasy.booking_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.singeasy.booking_service.dto.req.RoomReqDto;
import com.singeasy.booking_service.dto.res.RoomResDto;
import com.singeasy.booking_service.service.RoomService;

@RestController
@RequestMapping("/api/v1/")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    // POST /api/v1/shops/1/rooms
    @PostMapping("/shops/{shopId}/rooms")
    public ResponseEntity<RoomResDto> createRoom(
            @PathVariable Long shopId, 
            @RequestBody RoomReqDto roomDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(roomService.addRoomToShop(shopId, roomDto));
    }

    // PUT /api/v1/rooms/10
    @PutMapping("/rooms/{roomId}")
    public ResponseEntity<RoomResDto> updateRoom(
            @PathVariable Long roomId, 
            @RequestBody RoomReqDto roomDto) {
        return ResponseEntity.ok(roomService.updateRoom(roomId, roomDto));
    }

    // GET /api/v1/shops/1/rooms
    @GetMapping("/shops/{shopId}/rooms")
    public ResponseEntity<List<RoomResDto>> getRoomsByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(roomService.getRoomsByShop(shopId));
    }
}