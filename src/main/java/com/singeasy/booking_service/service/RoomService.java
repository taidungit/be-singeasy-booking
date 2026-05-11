package com.singeasy.booking_service.service;

import com.singeasy.booking_service.dto.req.RoomReqDto;
import com.singeasy.booking_service.dto.res.RoomResDto;
import com.singeasy.booking_service.entity.KaraokeShop;
import com.singeasy.booking_service.entity.Room;
import com.singeasy.booking_service.repository.RoomRepository;
import com.singeasy.booking_service.repository.ShopRepository;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final ShopRepository shopRepository;
    private final ModelMapper modelMapper;

    public RoomService(RoomRepository roomRepository, ShopRepository shopRepository, ModelMapper modelMapper) {
        this.roomRepository = roomRepository;
        this.shopRepository = shopRepository;
        this.modelMapper = modelMapper;
    }

    // 1. Thêm phòng: Gán shopId từ URL vào Entity
    public RoomResDto addRoomToShop(Long shopId, RoomReqDto roomDto) {
        KaraokeShop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop không tồn tại với id: " + shopId));

        Room room = modelMapper.map(roomDto, Room.class);
        room.setShop(shop); // Chốt cứng Shop từ Path Variable
        
        Room savedRoom = roomRepository.save(room);
        return convertToResDto(savedRoom);
    }

    // 2. Cập nhật phòng: Chỉ đè dữ liệu mới lên bản ghi cũ
    public RoomResDto updateRoom(Long roomId, RoomReqDto roomDto) {
        Room existingRoom = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại với id: " + roomId));

        // Map dữ liệu từ DTO đè lên Entity cũ (giữ nguyên id và shop)
        modelMapper.map(roomDto, existingRoom);
        
        Room updatedRoom = roomRepository.save(existingRoom);
        return convertToResDto(updatedRoom);
    }

    // 3. Lấy tất cả phòng của Shop
    public List<RoomResDto> getRoomsByShop(Long shopId) {
        List<Room> rooms = roomRepository.findByShopId(shopId);
        return rooms.stream()
                .map(this::convertToResDto)
                .toList();
    }

    public void deleteRoom(Long roomId) {
        roomRepository.deleteById(roomId);
    }

    // Hàm bổ trợ để map thủ công các trường Shop
    private RoomResDto convertToResDto(Room room) {
        RoomResDto res = modelMapper.map(room, RoomResDto.class);
        if (room.getShop() != null) {
            res.setShopId(room.getShop().getId());
            res.setShopName(room.getShop().getName());
        }
        return res;
    }
}