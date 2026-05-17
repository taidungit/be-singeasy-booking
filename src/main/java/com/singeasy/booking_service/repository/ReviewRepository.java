package com.singeasy.booking_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.singeasy.booking_service.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByKaraokeShopIdOrderByCreatedAtDesc(Long shopId);
}
