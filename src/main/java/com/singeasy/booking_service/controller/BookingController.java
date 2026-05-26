package com.singeasy.booking_service.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.singeasy.booking_service.dto.OccupiedSlotDto;
import com.singeasy.booking_service.dto.req.BookingReqDto;
import com.singeasy.booking_service.dto.res.BookingResDto;
import com.singeasy.booking_service.entity.Booking;
import com.singeasy.booking_service.entity.User;
import com.singeasy.booking_service.repository.BookingRepository;
import com.singeasy.booking_service.service.BookingService;
import com.singeasy.booking_service.service.UserService;
import com.singeasy.booking_service.util.SecurityUtil;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;
    private final BookingRepository bookingRepository;

    @PostMapping
    public ResponseEntity<BookingResDto> create(@RequestBody BookingReqDto request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    // Cancel booking using PutMapping
    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResDto> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }

    // Get personal booking history
    @GetMapping("/history")
    public ResponseEntity<?> getHistory() {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        if (email.isEmpty()) return ResponseEntity.status(401).build();
        
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(bookingService.getUserHistory(user.getId()));
    }

    @GetMapping("/shop/{shopId}")
        public ResponseEntity<List<BookingResDto>> getBookingsByShop(@PathVariable Long shopId) {
            return ResponseEntity.ok(bookingService.getBookingsByShopId(shopId));
        }


    @GetMapping
    public ResponseEntity<List<BookingResDto>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    // 2. API Admin duyệt đơn phòng (Chuyển trạng thái sang CONFIRMED)
    // Endpoint thực tế: PUT /api/v1/bookings/{id}/approve
    @PutMapping("/{id}/approve")
    public ResponseEntity<BookingResDto> approveBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.approveBooking(id));
    }

    @GetMapping("/occupied-slots")
    public List<OccupiedSlotDto> getOccupiedSlots(Long roomId, LocalDate date) {
    List<Booking> activeBookings = bookingRepository.findActiveBookingsByRoomAndDate(roomId, date);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    return activeBookings.stream().map(booking -> {
        LocalDateTime startDT = LocalDateTime.of(booking.getBookingDate(), booking.getStartTime());
        LocalDateTime endDT = startDT.plusHours(booking.getDuration());
        
        String startTimeStr = startDT.format(formatter);
        String endTimeStr;
        
        // 🟢 NẾU GIỜ KẾT THÚC SANG NGÀY HÔM SAU (Ví dụ: 00:00 hoặc 01:00 sáng)
        if (endDT.toLocalDate().isAfter(date)) {
            // Ép chuỗi hiển thị thành "24:00" hoặc mốc kịch khung để Frontend hiểu là khóa đến hết đêm
            endTimeStr = "24:00"; 
        } else {
            endTimeStr = endDT.format(formatter);
        }
        
        return new OccupiedSlotDto(startTimeStr, endTimeStr);
    }).toList();
}
}
