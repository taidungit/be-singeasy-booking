package com.singeasy.booking_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.singeasy.booking_service.entity.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    public List<Room> findByShopId(Long id);

    @Query("SELECT DISTINCT r.capacity FROM Room r WHERE r.capacity IS NOT NULL")
    List<String> findDistinctCapacities();
}
