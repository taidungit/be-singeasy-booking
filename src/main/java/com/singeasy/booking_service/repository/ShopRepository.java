package com.singeasy.booking_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.singeasy.booking_service.entity.KaraokeShop;
import com.singeasy.booking_service.enums.ShopStatus;

@Repository
public interface ShopRepository extends JpaRepository<KaraokeShop, Long> {
    // Tìm tất cả các shop chưa bị xóa (Status khác DELETED)
    List<KaraokeShop> findByStatusNot(ShopStatus status);

    // Tìm một shop cụ thể theo ID và phải chưa bị xóa
    Optional<KaraokeShop> findByIdAndStatusNot(Long id, ShopStatus status);

    @Query("SELECT DISTINCT s.city FROM KaraokeShop s WHERE s.city IS NOT NULL AND s.city != ''")
    List<String> findDistinctCities();

    @Query(value = "SELECT * FROM karaoke_shop s WHERE s.status != 'DELETED' " +
            "AND (:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:address IS NULL OR LOWER(s.address) LIKE LOWER(CONCAT('%', :address, '%'))) " +
            "AND (:minRating IS NULL OR s.rating >= :minRating) " +
            "AND (:minPrice IS NULL OR s.min_price_per_hour >= :minPrice) " +
            "AND (:maxPrice IS NULL OR s.min_price_per_hour <= :maxPrice)", 
            nativeQuery = true)
    List<KaraokeShop> findFilteredShopsNative(
            @Param("name") String name,
            @Param("address") String address,
            @Param("minRating") Double minRating,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice
    );
}

