package com.singeasy.booking_service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
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

import jakarta.transaction.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository, ModelMapper modelMapper) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.modelMapper = modelMapper;
    }
    @Transactional
    public BookingResDto createBooking(BookingReqDto dto, User currentUser) {
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.getStatus().equals(RoomStatus.AVAILABLE.name())) {
            throw new RuntimeException("Room is not available");
        }

        Booking booking = new Booking();
        // Manual mapping or use modelMapper.map(dto, booking)
        booking.setBookingDate(dto.getBookingDate());
        booking.setStartTime(dto.getStartTime());
        booking.setDuration(dto.getDuration());
        booking.setServiceFee(dto.getServiceFee());
        booking.setPricePerHour(room.getPricePerHour());
        
        // Final price calculation
        double total = (room.getPricePerHour() * dto.getDuration()) + dto.getServiceFee();
        booking.setTotalAmount(total);
        
        booking.setStatus(BookingStatusEnum.CONFIRMED);
        booking.setUser(currentUser);
        booking.setRoom(room);

        // Update room status
        room.setStatus(RoomStatus.OCCUPIED.name());
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
        BookingResDto res = modelMapper.map(booking, BookingResDto.class);
        
        // Ensure manual fields are set if modelMapper misses them
        if (booking.getRoom() != null) {
            res.setRoomId(booking.getRoom().getId());
            res.setRoomName(booking.getRoom().getName());
        }
        if (booking.getUser() != null) {
            res.setUserName(booking.getUser().getName());
            res.setUserEmail(booking.getUser().getEmail());
        }
        
        return res;
    }
}