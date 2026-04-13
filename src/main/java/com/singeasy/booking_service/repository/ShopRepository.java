package com.singeasy.booking_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.singeasy.booking_service.entity.KaraokeShop;
import com.singeasy.booking_service.enums.ShopStatus;

@Repository
public interface ShopRepository extends JpaRepository<KaraokeShop, Long> {
    // Tìm tất cả các shop chưa bị xóa (Status khác DELETED)
    List<KaraokeShop> findByStatusNot(ShopStatus status);

    // Tìm một shop cụ thể theo ID và phải chưa bị xóa
    Optional<KaraokeShop> findByIdAndStatusNot(Long id, ShopStatus status);
}
