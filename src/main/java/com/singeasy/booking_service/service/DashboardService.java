package com.singeasy.booking_service.service;

import com.singeasy.booking_service.dto.res.DashboardSummaryRes;
import com.singeasy.booking_service.entity.KaraokeShop;
import com.singeasy.booking_service.entity.Room;
import com.singeasy.booking_service.entity.User;
import com.singeasy.booking_service.enums.BookingStatusEnum;
import com.singeasy.booking_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ShopRepository shopRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository; 
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    public DashboardSummaryRes getDashboardSummary() {
        // 1. Lấy dữ liệu cho phần Stats
        long totalShops = shopRepository.count();
        long totalRooms = roomRepository.count();
        long totalUsers = userRepository.count(); 
        long pendingBookings = bookingRepository.countByStatus(BookingStatusEnum.PENDING);
        
        DashboardSummaryRes.Stats stats = DashboardSummaryRes.Stats.builder()
                .totalShops(totalShops)
                .totalRooms(totalRooms)
                .totalUsers(totalUsers)
                .pendingBookings(pendingBookings)
                .build();

        // 2. Tính toán doanh thu 7 ngày gần nhất (Thứ 2 -> Chủ nhật)
        List<DashboardSummaryRes.ChartData> chartData = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = 6; i >= 0; i--) {
            LocalDate targetDate = today.minusDays(i);
            Double revenue = bookingRepository.calculateRevenueByDate(targetDate);
            
            String dayName = targetDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            chartData.add(DashboardSummaryRes.ChartData.builder()
                    .name(dayName)
                    .revenue(revenue != null ? revenue : 0.0)
                    .build());
        }

        List<DashboardSummaryRes.RecentActivity> recentActivities = bookingRepository.findTop5ByOrderByCreatedAtDesc()
        .stream()
        .map(b -> {
            // Lấy tên khách hàng an toàn
            String customerName = Optional.ofNullable(b.getUser())
                    .map(User::getName)
                    .orElse("Guest User");

            // Lấy tên phòng an toàn
            String roomName = Optional.ofNullable(b.getRoom())
                    .map(Room::getName)
                    .orElse("a room");

            String formattedDateTime = b.getCreatedAt() != null 
                    ? b.getCreatedAt().format(DATE_TIME_FORMATTER) 
                    : "--:--";

            // Lấy tên chi nhánh (Shop) xuyên suốt từ Room an toàn
            String shopName = Optional.ofNullable(b.getRoom())
                    .map(Room::getShop)
                    .map(KaraokeShop::getName)
                    .orElse("SingEasy Venue");

            return DashboardSummaryRes.RecentActivity.builder()
                    .id(b.getId())
                    .description(String.format("Customer %s booked %s", customerName, roomName))
                    .timeAgo(formattedDateTime) 
                    .branchName(shopName)
                    .build();
        })
        .toList(); 
        return DashboardSummaryRes.builder()
                .stats(stats)
                .chartData(chartData)
                .recentActivities(recentActivities)
                .build();
    }
}