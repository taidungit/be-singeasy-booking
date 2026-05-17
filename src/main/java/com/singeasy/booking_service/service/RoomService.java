package com.singeasy.booking_service.service;

import com.singeasy.booking_service.dto.req.RoomReqDto;
import com.singeasy.booking_service.dto.res.RoomResDto;
import com.singeasy.booking_service.entity.Amenity;
import com.singeasy.booking_service.entity.KaraokeShop;
import com.singeasy.booking_service.entity.Room;
import com.singeasy.booking_service.enums.RoomStatus;
import com.singeasy.booking_service.repository.AmenityRepository;
import com.singeasy.booking_service.repository.RoomRepository;
import com.singeasy.booking_service.repository.ShopRepository;

import jakarta.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final ShopRepository shopRepository;
    private final ModelMapper modelMapper;
    private final AmenityRepository amenityRepository;

    public RoomService(RoomRepository roomRepository, ShopRepository shopRepository, ModelMapper modelMapper, AmenityRepository amenityRepository) {
        this.roomRepository = roomRepository;
        this.shopRepository = shopRepository;
        this.modelMapper = modelMapper;
        this.amenityRepository = amenityRepository;
    }

    @Transactional
    public RoomResDto addRoomToShop(Long shopId, RoomReqDto dto) {
        KaraokeShop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        Room room = modelMapper.map(dto, Room.class);
        room.setShop(shop);
        room.setStatus(RoomStatus.AVAILABLE.name()); 

        // XỬ LÝ AMENITIES Ở ĐÂY:
        if (dto.getAmenities() != null) {
            List<Amenity> existingAmenities = dto.getAmenities().stream()
                .map(name -> amenityRepository.findByName(name)
                    .orElseThrow(() -> new RuntimeException("Amenity not found: " + name)))
                .toList();
            room.setAmenities(existingAmenities);
        }

        return convertToResDto(roomRepository.save(room));
    }

    public RoomResDto getRoomById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));
        if (room.getStatus().equals(RoomStatus.DELETED.name())) {
            throw new RuntimeException("Room has been deleted");
        }
        return convertToResDto(room);
    }
    @Transactional
    public RoomResDto updateRoom(Long roomId, RoomReqDto roomDto) {
        Room existingRoom = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));
        modelMapper.map(roomDto, existingRoom);

        if (roomDto.getAmenities() != null) {
            existingRoom.getAmenities().clear(); 
            
            List<Amenity> updatedAmenities = roomDto.getAmenities().stream()
                    .map(name -> amenityRepository.findByName(name)
                            .orElseThrow(() -> new RuntimeException("Amenity not found: " + name)))
                    .toList();
            existingRoom.getAmenities().addAll(updatedAmenities);
        } else {
            existingRoom.getAmenities().clear();
        }

        // 4. Lưu lại
        Room updatedRoom = roomRepository.save(existingRoom);
        return convertToResDto(updatedRoom);
    }

    // 3. Lấy tất cả phòng của Shop
    public List<RoomResDto> getRoomsByShop(Long shopId) {
            List<Room> rooms = roomRepository.findByShopId(shopId);
            return rooms.stream()
                    .filter(room -> !room.getStatus().equals(RoomStatus.DELETED.name())) // Thêm check ở đây
                    .map(this::convertToResDto)
                    .toList();
        }

    @Transactional
    public void deleteRoom(Long roomId) {
        // Thay ResourceNotFoundException thành RuntimeException cho đồng bộ
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        room.setStatus(RoomStatus.DELETED.name()); 
        roomRepository.save(room);
    }

    // Hàm bổ trợ để map thủ công các trường Shop
    private RoomResDto convertToResDto(Room room) {
        RoomResDto dto = new RoomResDto();
        dto.setId(room.getId());
        dto.setName(room.getName());
        dto.setCapacity(room.getCapacity());
        dto.setPricePerHour(room.getPricePerHour());
        dto.setImageUrl(room.getImageUrl());
        dto.setStatus(room.getStatus()); 

        List<String> amenityNames = room.getAmenities().stream()
                .map(Amenity::getName) // Giả sử thực thể Amenity có hàm getName()
                .toList();
        dto.setAmenities(amenityNames);

        return dto;
    }
}