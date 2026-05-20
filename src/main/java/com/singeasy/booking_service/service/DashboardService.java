package com.singeasy.booking_service.service;

import com.singeasy.booking_service.dto.res.DashboardSummaryRes;
import com.singeasy.booking_service.enums.BookingStatusEnum;
import com.singeasy.booking_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ShopRepository shopRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository; 

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

        // 3. Lấy danh sách hoạt động gần đây (Recent Bookings) - Bọc chống Null an toàn
        List<DashboardSummaryRes.RecentActivity> recentActivities = bookingRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(b -> {
                    // Kiểm tra an toàn cho thực thể User liên kết
                    String customerName = (b.getUser() != null && b.getUser().getName() != null) 
                            ? b.getUser().getName() 
                            : "Guest User";

                    // Kiểm tra an toàn cho thực thể Room liên kết
                    String roomName = (b.getRoom() != null && b.getRoom().getName() != null) 
                            ? b.getRoom().getName() 
                            : "a room";

                    // Kiểm tra an toàn cho thực thể Shop liên kết xuyên suốt từ Room
                    String shopName = "SingEasy Venue";
                    if (b.getRoom() != null && b.getRoom().getShop() != null && b.getRoom().getShop().getName() != null) {
                        shopName = b.getRoom().getShop().getName();
                    }

                    return DashboardSummaryRes.RecentActivity.builder()
                            .id(b.getId())
                            .description("Customer " + customerName + " booked " + roomName)
                            .timeAgo("Recent") 
                            .branchName(shopName)
                            .build();
                })
                .collect(Collectors.toList());

        return DashboardSummaryRes.builder()
                .stats(stats)
                .chartData(chartData)
                .recentActivities(recentActivities)
                .build();
    }
}