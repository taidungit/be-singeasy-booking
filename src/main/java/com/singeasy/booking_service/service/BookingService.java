package com.singeasy.booking_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.singeasy.booking_service.dto.req.BookingReqDto;
import com.singeasy.booking_service.dto.res.BookingResDto;
import com.singeasy.booking_service.entity.Booking;
import com.singeasy.booking_service.entity.Room;
import com.singeasy.booking_service.entity.User;
import com.singeasy.booking_service.enums.BookingStatusEnum;
import com.singeasy.booking_service.enums.RoomStatus;
import com.singeasy.booking_service.repository.BookingRepository;
import com.singeasy.booking_service.repository.RoomRepository;
import com.singeasy.booking_service.repository.UserRepository;
import com.singeasy.booking_service.util.SecurityUtil;

import jakarta.transaction.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

@Transactional
public BookingResDto createBooking(BookingReqDto dto) { // Bỏ tham số User currentUser cũ
    Room room = roomRepository.findById(dto.getRoomId())
            .orElseThrow(() -> new RuntimeException("Room not found"));
            
    if (!room.getStatus().equals(RoomStatus.AVAILABLE.name())) {
        throw new RuntimeException("Room is not available");
    }

    Booking booking = new Booking();
    booking.setBookingDate(dto.getBookingDate());
    booking.setStartTime(dto.getStartTime());
    booking.setDuration(dto.getDuration());
    booking.setServiceFee(dto.getServiceFee());
    booking.setPricePerHour(room.getPricePerHour());
    
    double total = (room.getPricePerHour() * dto.getDuration()) + dto.getServiceFee();
    booking.setTotalAmount(total);
    booking.setCreatedAt(LocalDateTime.now());
    booking.setStatus(BookingStatusEnum.PENDING); // Mặc định là PENDING khi tạo mới
    booking.setRoom(room);

    String email = SecurityUtil.getCurrentUserLogin()
            .orElseThrow(() -> new RuntimeException("User not authenticated"));
            

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
            
    booking.setUser(user); // Gán User thật tìm được vào đây!


    if (room.getShop() != null) {
        booking.setShopId(room.getShop().getId());
        booking.setShopName(room.getShop().getName());
    }
    booking.setRoomName(room.getName());

    booking.setCreatedAt(java.time.LocalDateTime.now());

    room.setStatus(RoomStatus.BOOKED.name());
    roomRepository.save(room);

    Booking savedBooking = bookingRepository.save(booking);
    return convertToResDto(savedBooking);
}

    @Transactional
    public BookingResDto cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // 1. Update Booking status to CANCELLED
        booking.setStatus(BookingStatusEnum.CANCELLED);

        // 2. Release the room back to AVAILABLE
        Room room = booking.getRoom();
        room.setStatus(RoomStatus.AVAILABLE.name());
        roomRepository.save(room);

        return convertToResDto(bookingRepository.save(booking));
    }

    public List<BookingResDto> getUserHistory(Long userId) {
        return bookingRepository.findByUserIdOrderByBookingDateDesc(userId)
                .stream()
                .map(this::convertToResDto)
                .collect(Collectors.toList());
    }

private BookingResDto convertToResDto(Booking booking) {
    if (booking == null) {
        return null;
    }

    BookingResDto res = new BookingResDto();
    
    // 1. Map các trường dữ liệu nguyên bản từ bảng Booking
    res.setId(booking.getId());
    res.setBookingDate(booking.getBookingDate());
    res.setStartTime(booking.getStartTime());
    res.setDuration(booking.getDuration());
    res.setPricePerHour(booking.getPricePerHour());
    res.setServiceFee(booking.getServiceFee());
    res.setTotalAmount(booking.getTotalAmount());
    res.setStatus(booking.getStatus());
    res.setCreatedAt(booking.getCreatedAt()); // Sửa dứt điểm lỗi Invalid Date FE
    
    // 2. Map các trường tĩnh được lưu trực tiếp trong Booking
    res.setShopId(booking.getShopId());
    res.setShopName(booking.getShopName());
    res.setRoomName(booking.getRoomName()); // Khớp chuẩn trường tĩnh né lỗi xung đột

    // 3. Bốc thêm thông tin liên kết từ bảng Room (Nếu có liên kết thực thể)
    if (booking.getRoom() != null) {
        res.setRoomId(booking.getRoom().getId());
        // Nếu trường roomName tĩnh ở trên bị null, ta lấy fallback từ thực thể liên kết sang
        if (res.getRoomName() == null) {
            res.setRoomName(booking.getRoom().getName());
        }
    }
    
    // 4. Bốc thêm thông tin liên kết từ bảng User để đổ ra hóa đơn
    if (booking.getUser() != null) {
        res.setUserName(booking.getUser().getName());
        res.setUserEmail(booking.getUser().getEmail());
    }

    return res;
}



    @Transactional
    public List<BookingResDto> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::convertToResDto)
                .collect(Collectors.toList());
    }

    // 🌟 Xử lý duyệt đơn PENDING -> CONFIRMED
    @Transactional
    public BookingResDto approveBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking reservation not found"));

        // Chuyển trạng thái đơn thành CONFIRMED
        booking.setStatus(BookingStatusEnum.CONFIRMED);
        
        if (booking.getRoom() != null) {
            Room room = booking.getRoom();
            room.setStatus(RoomStatus.BOOKED.name());
            roomRepository.save(room);
        }

        Booking updatedBooking = bookingRepository.save(booking);
        return convertToResDto(updatedBooking);
    }
}