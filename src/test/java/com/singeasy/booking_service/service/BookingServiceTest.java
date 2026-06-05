package com.singeasy.booking_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.singeasy.booking_service.dto.req.BookingReqDto;
import com.singeasy.booking_service.dto.res.BookingResDto;
import com.singeasy.booking_service.entity.Booking;
import com.singeasy.booking_service.entity.KaraokeShop;
import com.singeasy.booking_service.entity.Room;
import com.singeasy.booking_service.entity.User;
import com.singeasy.booking_service.enums.BookingStatusEnum;
import com.singeasy.booking_service.enums.RoomStatus;
import com.singeasy.booking_service.repository.BookingRepository;
import com.singeasy.booking_service.repository.RoomRepository;
import com.singeasy.booking_service.repository.UserRepository;
import com.singeasy.booking_service.util.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void createBooking_savesPendingBooking() {
        BookingReqDto request = new BookingReqDto();
        request.setRoomId(1L);
        request.setBookingDate(LocalDate.of(2026, 6, 5));
        request.setStartTime(LocalTime.of(18, 0));
        request.setDuration(2);
        request.setServiceFee(10.0);

        KaraokeShop shop = new KaraokeShop();
        shop.setId(1L);
        shop.setName("Shop A");

        Room room = new Room();
        room.setId(1L);
        room.setName("VIP 1");
        room.setPricePerHour(100.0);
        room.setStatus(RoomStatus.AVAILABLE.name());
        room.setShop(shop);

        User user = new User();
        user.setId(5L);
        user.setEmail("user@test.com");
        user.setName("Test User");

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(bookingRepository.findActiveBookingsByRoomAndDate(1L, request.getBookingDate()))
                .thenReturn(List.of());
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(10L);
            return booking;
        });

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserLogin).thenReturn(Optional.of("user@test.com"));

            BookingResDto result = bookingService.createBooking(request);

            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getStatus()).isEqualTo(BookingStatusEnum.PENDING);
            assertThat(result.getTotalAmount()).isEqualTo(210.0);
            assertThat(result.getShopName()).isEqualTo("Shop A");
            assertThat(result.getRoomName()).isEqualTo("VIP 1");
        }
    }

    @Test
    void createBooking_throws_whenTimeSlotOverlaps() {
        BookingReqDto request = new BookingReqDto();
        request.setRoomId(1L);
        request.setBookingDate(LocalDate.of(2026, 6, 5));
        request.setStartTime(LocalTime.of(18, 0));
        request.setDuration(2);
        request.setServiceFee(0.0);

        Room room = new Room();
        room.setId(1L);
        room.setPricePerHour(100.0);
        room.setStatus(RoomStatus.AVAILABLE.name());

        Booking existing = new Booking();
        existing.setBookingDate(LocalDate.of(2026, 6, 5));
        existing.setStartTime(LocalTime.of(17, 0));
        existing.setDuration(3);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(bookingRepository.findActiveBookingsByRoomAndDate(1L, request.getBookingDate()))
                .thenReturn(List.of(existing));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Khung giờ này đã có người đặt trước!");
    }

    @Test
    void createBooking_throws_whenRoomDeleted() {
        BookingReqDto request = new BookingReqDto();
        request.setRoomId(1L);

        Room room = new Room();
        room.setStatus(RoomStatus.DELETED.name());

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Phòng này hiện không còn hoạt động.");
    }

    @Test
    void cancelBooking_setsStatusToCancelled() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatusEnum.PENDING);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingResDto result = bookingService.cancelBooking(1L);

        assertThat(booking.getStatus()).isEqualTo(BookingStatusEnum.CANCELLED);
        assertThat(result.getStatus()).isEqualTo(BookingStatusEnum.CANCELLED);
    }

    @Test
    void approveBooking_setsStatusToConfirmed() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatusEnum.PENDING);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingResDto result = bookingService.approveBooking(1L);

        assertThat(booking.getStatus()).isEqualTo(BookingStatusEnum.CONFIRMED);
        assertThat(result.getStatus()).isEqualTo(BookingStatusEnum.CONFIRMED);
    }

    @Test
    void getUserHistory_returnsBookingsForUser() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatusEnum.CONFIRMED);

        when(bookingRepository.findByUserIdOrderByBookingDateDesc(5L)).thenReturn(List.of(booking));

        List<BookingResDto> result = bookingService.getUserHistory(5L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(BookingStatusEnum.CONFIRMED);
    }

    @Test
    void getBookingsByShopId_mapsBookingDetails() {
        User user = new User();
        user.setName("Customer");
        user.setEmail("customer@test.com");

        KaraokeShop shop = new KaraokeShop();
        shop.setId(1L);
        shop.setName("Shop A");

        Room room = new Room();
        room.setId(2L);
        room.setName("Room A");
        room.setShop(shop);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatusEnum.PENDING);
        booking.setUser(user);
        booking.setRoom(room);

        when(bookingRepository.findByRoomByShopId(1L)).thenReturn(List.of(booking));

        List<BookingResDto> result = bookingService.getBookingsByShopId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserName()).isEqualTo("Customer");
        assertThat(result.get(0).getRoomName()).isEqualTo("Room A");
        assertThat(result.get(0).getShopName()).isEqualTo("Shop A");
    }
}
