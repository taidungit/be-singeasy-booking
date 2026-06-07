package com.singeasy.booking_service.service;

import com.singeasy.booking_service.dto.req.RoomReqDto;
import com.singeasy.booking_service.dto.res.RoomResDto;
import com.singeasy.booking_service.entity.Amenity;
import com.singeasy.booking_service.entity.KaraokeShop;
import com.singeasy.booking_service.entity.Room;
import com.singeasy.booking_service.enums.RoomStatus;
import com.singeasy.booking_service.repository.AmenityRepository;
import com.singeasy.booking_service.repository.BookingRepository;
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
    private final BookingRepository bookingRepository;

    public RoomService(RoomRepository roomRepository, ShopRepository shopRepository, ModelMapper modelMapper, AmenityRepository amenityRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.shopRepository = shopRepository;
        this.modelMapper = modelMapper;
        this.amenityRepository = amenityRepository;
        this.bookingRepository = bookingRepository;
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

// Hàm bổ trợ để map thủ công các trường Shop - ĐÃ CẬP NHẬT KIỂM TRA 2 NGÀY
// private RoomResDto convertToResDto(Room room) {
//     RoomResDto dto = new RoomResDto();
//     dto.setId(room.getId());
//     dto.setName(room.getName());
//     dto.setCapacity(room.getCapacity());
//     dto.setPricePerHour(room.getPricePerHour());
//     dto.setImageUrl(room.getImageUrl());
//     dto.setStatus(room.getStatus()); 

//     List<String> amenityNames = room.getAmenities().stream()
//             .map(Amenity::getName)
//             .toList();
//     dto.setAmenities(amenityNames);

//     // Mốc thời gian tính toán
//     java.time.LocalDate today = java.time.LocalDate.now();
//     java.time.LocalDate tomorrow = today.plusDays(1);
            
//     // 1. 🟢 TÍNH TOÁN NGÀY HÔM NAY (Cộng dồn cả giờ quá khứ đã trôi qua)
//     List<com.singeasy.booking_service.entity.Booking> todayBookings = 
//             bookingRepository.findActiveBookingsByRoomAndDate(room.getId(), today);
            
//     int todayBookedHours = todayBookings.stream()
//             .mapToInt(com.singeasy.booking_service.entity.Booking::getDuration)
//             .sum();

//     // Tính số giờ đã trôi qua trong ngày hôm nay (Bắt đầu đếm từ mốc quán mở cửa là 12:00)
//     int currentHour = java.time.LocalTime.now().getHour();
//     int todayPassedHours = 0;
//     if (currentHour >= 12) {
//         todayPassedHours = Math.min(12, currentHour - 12); 
//     }

//     // Ngày hôm nay hết slot khi: Số giờ đã hát + Số giờ đã trôi qua quá khứ >= 12 tiếng
//     boolean isTodayFull = (todayBookedHours + todayPassedHours) >= 12;


//     // 2. 🟢 TÍNH TOÁN NGÀY MAI (Ngày mai chưa diễn ra nên chỉ check tổng giờ đặt)
//     List<com.singeasy.booking_service.entity.Booking> tomorrowBookings = 
//             bookingRepository.findActiveBookingsByRoomAndDate(room.getId(), tomorrow);
            
//     int tomorrowBookedHours = tomorrowBookings.stream()
//             .mapToInt(com.singeasy.booking_service.entity.Booking::getDuration)
//             .sum();

//     boolean isTomorrowFull = tomorrowBookedHours >= 12;


//     // 3. 💥 CHỐT ĐIỀU KIỆN: Chỉ khi CẢ HÔM NAY VÀ NGÀY MAI đều không còn slot nào thì mới gán FullyBooked = true
//     dto.setFullyBooked(isTodayFull && isTomorrowFull);

//     return dto;
// }


private RoomResDto convertToResDto(Room room) {
    RoomResDto dto = new RoomResDto();
    dto.setId(room.getId());
    dto.setName(room.getName());
    dto.setCapacity(room.getCapacity());
    dto.setPricePerHour(room.getPricePerHour());
    dto.setImageUrl(room.getImageUrl());
    dto.setStatus(room.getStatus()); 

    List<String> amenityNames = room.getAmenities().stream()
            .map(Amenity::getName)
            .toList();
    dto.setAmenities(amenityNames);

    // Mốc thời gian tính toán
    java.time.LocalDate today = java.time.LocalDate.now();
    java.time.LocalDate tomorrow = today.plusDays(1);
            
    // 1. 🟢 TÍNH TOÁN NGÀY HÔM NAY
    List<com.singeasy.booking_service.entity.Booking> todayBookings = 
            bookingRepository.findActiveBookingsByRoomAndDate(room.getId(), today);
            
    int todayBookedHours = todayBookings.stream()
            .mapToInt(com.singeasy.booking_service.entity.Booking::getDuration)
            .sum();

    // Lấy giờ hiện tại của hệ thống
    int currentHour = java.time.LocalTime.now().getHour();
    int todayPassedHours = 0;
    
    // Theo giao diện: Quán hoạt động từ 12:00 đến 24:00 (Hết mốc 23:00 là sang 24:00)
    if (currentHour >= 24) {
        todayPassedHours = 12; // Nếu đã qua ngày mới/quá nửa đêm thì xem như hôm nay hết sạch slot
    } else if (currentHour >= 12) {
        todayPassedHours = currentHour - 12; // Số giờ thực tế đã trôi qua tính từ lúc 12h trưa
    }

    // Ngày hôm nay hết slot khi: Số giờ đã hát + Số giờ quá khứ đã trôi qua >= 12 tiếng
    // Hoặc thời gian hiện tại đã từ 23h trở đi (mốc giờ cuối cùng trong dropdown là 23:00, sau đó không đặt được nữa)
    boolean isTodayFull = (todayBookedHours + todayPassedHours) >= 12 || currentHour >= 23;


    // 2. 🟢 TÍNH TOÁN NGÀY MAI
    List<com.singeasy.booking_service.entity.Booking> tomorrowBookings = 
            bookingRepository.findActiveBookingsByRoomAndDate(room.getId(), tomorrow);
            
    int tomorrowBookedHours = tomorrowBookings.stream()
            .mapToInt(com.singeasy.booking_service.entity.Booking::getDuration)
            .sum();

    // Ngày mai chưa diễn ra nên chỉ hết chỗ khi tổng giờ khách đặt đủ 12 tiếng
    boolean isTomorrowFull = tomorrowBookedHours >= 12;


    // 3. 💥 CHỐT ĐIỀU KIỆN
    dto.setFullyBooked(isTodayFull && isTomorrowFull);

    return dto;
}

    public List<String> getDistinctRoomCapacities() {
    List<String> capacities = roomRepository.findDistinctCapacities();
        if (capacities.isEmpty()) {
            return List.of("small", "medium", "large");
        }
    return capacities;
}
}