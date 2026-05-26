package com.singeasy.booking_service.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import com.singeasy.booking_service.dto.OccupiedSlotDto;
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
    public BookingResDto createBooking(BookingReqDto dto) {
Room room = roomRepository.findById(dto.getRoomId())
            .orElseThrow(() -> new RuntimeException("Room not found"));
                
    if (room.getStatus().equals(RoomStatus.DELETED.name())) {
        throw new RuntimeException("Phòng này hiện không còn hoạt động.");
    }

    // 🟢 CHUYỂN SANG DÙNG LOCALDATETIME ĐỂ KHÔNG BỊ LỖI QUA ĐÊM (00:00)
    LocalDateTime newStartDT = LocalDateTime.of(dto.getBookingDate(), dto.getStartTime());
    LocalDateTime newEndDT = newStartDT.plusHours(dto.getDuration());

    List<Booking> activeBookings = bookingRepository.findActiveBookingsByRoomAndDate(room.getId(), dto.getBookingDate());

    for (Booking oldBooking : activeBookings) {
        LocalDateTime oldStartDT = LocalDateTime.of(oldBooking.getBookingDate(), oldBooking.getStartTime());
        LocalDateTime oldEndDT = oldStartDT.plusHours(oldBooking.getDuration());

        // Kiểm tra overlap bằng cả ngày lẫn giờ: NewStart < OldEnd VÀ NewEnd > OldStart
        if (newStartDT.isBefore(oldEndDT) && newEndDT.isAfter(oldStartDT)) {
            throw new RuntimeException("Khung giờ này đã có người đặt trước!");
        }
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
        booking.setStatus(BookingStatusEnum.PENDING); // Đơn mới luôn ở trạng thái PENDING
        booking.setRoom(room);

        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
                
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
                
        booking.setUser(user); 

        if (room.getShop() != null) {
            booking.setShopId(room.getShop().getId());
            booking.setShopName(room.getShop().getName());
        }
        booking.setRoomName(room.getName());
        Booking savedBooking = bookingRepository.save(booking);
        return convertToResDto(savedBooking);
    }

    @Transactional
    public BookingResDto cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Chuyển trạng thái đơn về CANCELLED để giải phóng các slot giờ khi truy vấn
        booking.setStatus(BookingStatusEnum.CANCELLED);

        // ❌ ĐÃ XÓA: Không gọi room.setStatus(RoomStatus.AVAILABLE) gây xung đột trạng thái tĩnh.

        return convertToResDto(bookingRepository.save(booking));
    }

    // 🌟 Xử lý duyệt đơn PENDING -> CONFIRMED
    @Transactional
    public BookingResDto approveBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking reservation not found"));

        // Chuyển trạng thái đơn thành CONFIRMED để chốt lịch
        booking.setStatus(BookingStatusEnum.CONFIRMED);
        
        // ❌ ĐÃ XÓA: Bỏ hoàn toàn việc gán cứng trạng thái Room tại đây để né bẫy logic khóa phòng.

        Booking updatedBooking = bookingRepository.save(booking);
        return convertToResDto(updatedBooking);
    }

    public List<BookingResDto> getUserHistory(Long userId) {
        return bookingRepository.findByUserIdOrderByBookingDateDesc(userId)
                .stream()
                .map(this::convertToResDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<BookingResDto> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::convertToResDto)
                .collect(Collectors.toList());
    }

    public List<BookingResDto> getBookingsByShopId(Long shopId) {
        List<Booking> bookings = bookingRepository.findByRoomByShopId(shopId);
        
        return bookings.stream()
                .map(booking -> {
                    BookingResDto dto = new BookingResDto();
                    dto.setId(booking.getId());
                    dto.setBookingDate(booking.getBookingDate());
                    dto.setStartTime(booking.getStartTime());
                    dto.setDuration(booking.getDuration());
                    dto.setTotalAmount(booking.getTotalAmount());
                    dto.setStatus(booking.getStatus());
                    dto.setCreatedAt(booking.getCreatedAt());
                    dto.setServiceFee(booking.getServiceFee());
                    dto.setPricePerHour(booking.getPricePerHour());
                    if (booking.getUser() != null) {
                        dto.setUserName(booking.getUser().getName());
                        dto.setUserEmail(booking.getUser().getEmail());
                    } else {
                        dto.setUserName("Guest User");
                        dto.setUserEmail("Unknown Email");
                    }
                    if (booking.getRoom() != null) {
                        dto.setRoomId(booking.getRoom().getId());
                        dto.setRoomName(booking.getRoom().getName());
                        if (booking.getRoom().getShop() != null) {
                            dto.setShopId(booking.getRoom().getShop().getId());
                            dto.setShopName(booking.getRoom().getShop().getName());
                        }
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<OccupiedSlotDto> getOccupiedSlots(Long roomId, LocalDate date) {
        List<Booking> activeBookings = bookingRepository.findActiveBookingsByRoomAndDate(roomId, date);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        return activeBookings.stream().map(booking -> {
            LocalTime start = booking.getStartTime();
            LocalTime end = start.plusHours(booking.getDuration());
            
            return new OccupiedSlotDto(
                start.format(formatter),
                end.format(formatter)
            );
        }).toList();
    }

    private BookingResDto convertToResDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        BookingResDto res = new BookingResDto();
        
        res.setId(booking.getId());
        res.setBookingDate(booking.getBookingDate());
        res.setStartTime(booking.getStartTime());
        res.setDuration(booking.getDuration());
        res.setPricePerHour(booking.getPricePerHour());
        res.setServiceFee(booking.getServiceFee());
        res.setTotalAmount(booking.getTotalAmount());
        res.setStatus(booking.getStatus());
        res.setCreatedAt(booking.getCreatedAt()); 
        
        res.setShopId(booking.getShopId());
        res.setShopName(booking.getShopName());
        res.setRoomName(booking.getRoomName()); 

        if (booking.getRoom() != null) {
            res.setRoomId(booking.getRoom().getId());
            if (res.getRoomName() == null) {
                res.setRoomName(booking.getRoom().getName());
            }
        }
        
        if (booking.getUser() != null) {
            res.setUserName(booking.getUser().getName());
            res.setUserEmail(booking.getUser().getEmail());
        }

        return res;
    }
}