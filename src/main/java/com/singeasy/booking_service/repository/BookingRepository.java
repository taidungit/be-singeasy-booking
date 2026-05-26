package com.singeasy.booking_service.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.singeasy.booking_service.entity.Booking;
import com.singeasy.booking_service.enums.BookingStatusEnum;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserIdOrderByBookingDateDesc(Long userId);
    @Query("SELECT b FROM Booking b WHERE b.room.shop.id = :shopId")
    List<Booking> findByRoomByShopId(@Param("shopId") Long shopId);

    // Đếm số đơn đang chờ xử lý (PENDING)
    long countByStatus(BookingStatusEnum status);

    // Tính tổng doanh thu của các đơn đã hoàn thành (CONFIRMED hoặc COMPLETED) theo ngày
    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.bookingDate = :date AND b.status = 'CONFIRMED'")
    Double calculateRevenueByDate(@Param("date") LocalDate date);

    // Lấy 5 đơn đặt phòng mới nhất để làm Hoạt động gần đây
    List<Booking> findTop5ByOrderByCreatedAtDesc();

    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId " +
       "AND b.bookingDate = :date " +
       "AND b.status != com.singeasy.booking_service.enums.BookingStatusEnum.CANCELLED")
    List<Booking> findActiveBookingsByRoomAndDate(
        @Param("roomId") Long roomId, 
        @Param("date") LocalDate date
);

}
