package com.singeasy.booking_service.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.singeasy.booking_service.entity.Booking;
import com.singeasy.booking_service.entity.Room;
import com.singeasy.booking_service.entity.User;
import com.singeasy.booking_service.enums.BookingStatusEnum;
import com.singeasy.booking_service.enums.ShopStatus;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Room room;
    private LocalDate bookingDate;

    @BeforeEach
    void setUp() {
        var shop = shopRepository.save(RepositoryTestSupport.shop("Shop A", "Hanoi", ShopStatus.ACTIVE));
        room = roomRepository.save(RepositoryTestSupport.room(shop, "VIP 1", "10"));
        user = userRepository.save(RepositoryTestSupport.user("Alice", "alice@test.com"));
        bookingDate = LocalDate.of(2026, 6, 5);
    }

    @Test
    void findByUserIdOrderByBookingDateDesc_returnsUserBookings() {
        saveBooking(LocalDate.of(2026, 6, 3), BookingStatusEnum.PENDING, 200.0);
        saveBooking(LocalDate.of(2026, 6, 7), BookingStatusEnum.CONFIRMED, 300.0);

        assertThat(bookingRepository.findByUserIdOrderByBookingDateDesc(user.getId()))
                .hasSize(2)
                .extracting(Booking::getBookingDate)
                .containsExactly(
                        LocalDate.of(2026, 6, 7),
                        LocalDate.of(2026, 6, 3));
    }

    @Test
    void findByRoomByShopId_returnsBookingsForShop() {
        saveBooking(bookingDate, BookingStatusEnum.PENDING, 200.0);

        var otherShop = shopRepository.save(RepositoryTestSupport.shop("Shop B", "Hue", ShopStatus.ACTIVE));
        var otherRoom = roomRepository.save(RepositoryTestSupport.room(otherShop, "Room B", "6"));
        bookingRepository.save(RepositoryTestSupport.booking(
                otherRoom, user, bookingDate, LocalTime.of(20, 0), 2, BookingStatusEnum.PENDING, 250.0));

        assertThat(bookingRepository.findByRoomByShopId(room.getShop().getId()))
                .hasSize(1)
                .first()
                .extracting(Booking::getRoomName)
                .isEqualTo("VIP 1");
    }

    @Test
    void countByStatus_countsMatchingBookings() {
        saveBooking(bookingDate, BookingStatusEnum.PENDING, 200.0);
        saveBooking(bookingDate.plusDays(1), BookingStatusEnum.PENDING, 220.0);
        saveBooking(bookingDate.plusDays(2), BookingStatusEnum.CONFIRMED, 240.0);

        assertThat(bookingRepository.countByStatus(BookingStatusEnum.PENDING)).isEqualTo(2);
        assertThat(bookingRepository.countByStatus(BookingStatusEnum.CONFIRMED)).isEqualTo(1);
    }

    @Test
    void calculateRevenueByDate_sumsConfirmedBookingsOnly() {
        saveBooking(bookingDate, BookingStatusEnum.CONFIRMED, 200.0);
        saveBooking(bookingDate, BookingStatusEnum.CONFIRMED, 150.0);
        saveBooking(bookingDate, BookingStatusEnum.PENDING, 100.0);

        assertThat(bookingRepository.calculateRevenueByDate(bookingDate)).isEqualTo(350.0);
    }

    @Test
    void findTop5ByOrderByCreatedAtDesc_returnsLatestBookings() {
        var oldest = saveBooking(bookingDate, BookingStatusEnum.PENDING, 100.0);
        oldest.setCreatedAt(LocalDateTime.of(2026, 6, 1, 10, 0));
        bookingRepository.save(oldest);

        var newest = saveBooking(bookingDate.plusDays(1), BookingStatusEnum.PENDING, 200.0);
        newest.setCreatedAt(LocalDateTime.of(2026, 6, 5, 18, 0));
        bookingRepository.save(newest);

        assertThat(bookingRepository.findTop5ByOrderByCreatedAtDesc())
                .hasSize(2)
                .first()
                .extracting(Booking::getTotalAmount)
                .isEqualTo(200.0);
    }

    @Test
    void findActiveBookingsByRoomAndDate_excludesCancelledBookings() {
        saveBooking(bookingDate, BookingStatusEnum.CONFIRMED, 200.0);
        saveBooking(bookingDate, BookingStatusEnum.CANCELLED, 150.0);

        assertThat(bookingRepository.findActiveBookingsByRoomAndDate(room.getId(), bookingDate))
                .hasSize(1)
                .first()
                .extracting(Booking::getStatus)
                .isEqualTo(BookingStatusEnum.CONFIRMED);
    }

    private Booking saveBooking(LocalDate date, BookingStatusEnum status, double totalAmount) {
        return bookingRepository.save(RepositoryTestSupport.booking(
                room, user, date, LocalTime.of(18, 0), 2, status, totalAmount));
    }
}
