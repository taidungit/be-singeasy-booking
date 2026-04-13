package com.singeasy.booking_service.service;

import com.singeasy.booking_service.entity.Room;
import com.singeasy.booking_service.repository.RoomRepository;
import com.singeasy.booking_service.repository.ShopRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final ShopRepository shopRepository; 

    public RoomService(RoomRepository roomRepository, ShopRepository shopRepository) {
        this.roomRepository = roomRepository;
        this.shopRepository = shopRepository;
    }

    // Lấy tất cả phòng của một Shop cụ thể
    public List<Room> getRoomsByShop(Long shopId) {
        return roomRepository.findByShopId(shopId);
    }

    // Thêm phòng vào một Shop
    public Room addRoomToShop(Long shopId, Room room) {
        return shopRepository.findById(shopId).map(shop -> {
            room.setShop(shop);
            return roomRepository.save(room);
        }).orElseThrow(() -> new RuntimeException("Shop không tồn tại với id: " + shopId));
    }

    // Cập nhật thông tin phòng
    public Room updateRoom(Long roomId, Room roomDetails) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại với id: " + roomId));
        
        // room.setRoomNumber(roomDetails.getRoomNumber());
        // room.setType(roomDetails.getType());
        room.setCapacity(roomDetails.getCapacity());
        room.setPricePerHour(roomDetails.getPricePerHour());
        room.setStatus(roomDetails.getStatus());
        
        return roomRepository.save(room);
    }

    // Xóa phòng
    public void deleteRoom(Long roomId) {
        roomRepository.deleteById(roomId);
    }

    public Room getRoomById(Long roomId) {
    return roomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng với ID: " + roomId));
}
}