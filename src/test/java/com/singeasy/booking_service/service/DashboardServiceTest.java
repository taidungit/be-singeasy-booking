package com.singeasy.booking_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.singeasy.booking_service.dto.res.DashboardSummaryRes;
import com.singeasy.booking_service.entity.Booking;
import com.singeasy.booking_service.entity.KaraokeShop;
import com.singeasy.booking_service.entity.Room;
import com.singeasy.booking_service.entity.User;
import com.singeasy.booking_service.enums.BookingStatusEnum;
import com.singeasy.booking_service.repository.BookingRepository;
import com.singeasy.booking_service.repository.RoomRepository;
import com.singeasy.booking_service.repository.ShopRepository;
import com.singeasy.booking_service.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getDashboardSummary_returnsStatsChartAndActivities() {
        when(shopRepository.count()).thenReturn(5L);
        when(roomRepository.count()).thenReturn(20L);
        when(userRepository.count()).thenReturn(100L);
        when(bookingRepository.countByStatus(BookingStatusEnum.PENDING)).thenReturn(3L);
        when(bookingRepository.calculateRevenueByDate(org.mockito.ArgumentMatchers.any()))
                .thenReturn(150.0);

        User customer = new User();
        customer.setName("Alice");

        KaraokeShop shop = new KaraokeShop();
        shop.setName("Shop A");

        Room room = new Room();
        room.setName("VIP 1");
        room.setShop(shop);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(customer);
        booking.setRoom(room);
        booking.setCreatedAt(LocalDateTime.now());

        when(bookingRepository.findTop5ByOrderByCreatedAtDesc()).thenReturn(List.of(booking));

        DashboardSummaryRes result = dashboardService.getDashboardSummary();

        assertThat(result.getStats().getTotalShops()).isEqualTo(5);
        assertThat(result.getStats().getTotalRooms()).isEqualTo(20);
        assertThat(result.getStats().getTotalUsers()).isEqualTo(100);
        assertThat(result.getStats().getPendingBookings()).isEqualTo(3);
        assertThat(result.getChartData()).hasSize(7);
        assertThat(result.getChartData().get(6).getRevenue()).isEqualTo(150.0);
        assertThat(result.getRecentActivities()).hasSize(1);
        assertThat(result.getRecentActivities().get(0).getDescription())
                .isEqualTo("Customer Alice booked VIP 1");
        assertThat(result.getRecentActivities().get(0).getBranchName()).isEqualTo("Shop A");
    }
}
