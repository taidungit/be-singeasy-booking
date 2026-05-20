package com.singeasy.booking_service.dto.res;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardSummaryRes {
    private Stats stats;
    private List<ChartData> chartData;
    private List<RecentActivity> recentActivities;

    @Data
    @Builder
    public static class Stats {
        private long totalShops;
        private long totalRooms;
        private long totalUsers;
        private long pendingBookings;

    }

    @Data
    @Builder
    public static class ChartData {
        private String name;
        private double revenue;
    }

    @Data
    @Builder
    public static class RecentActivity {
        private Long id;
        private String description;
        private String timeAgo;
        private String branchName;
    }
}