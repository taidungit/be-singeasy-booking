package com.singeasy.booking_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.singeasy.booking_service.entity.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
    public List<Room> findByShopId(Long id);
}
