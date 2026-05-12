package com.singeasy.booking_service.entity;


import java.util.List;

import com.singeasy.booking_service.enums.RoomStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name; 
    
    private String capacity; 
    
    private Double pricePerHour;
    
    @Enumerated(EnumType.STRING)
    private RoomStatus status;
    
    @Lob
    @Column(name = "image_url", columnDefinition = "LONGTEXT")
    private String imageUrl;

    @ManyToMany
    @JoinTable(
        name = "room_amenity",
        joinColumns = @JoinColumn(name = "room_id"),
        inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private List<Amenity> amenities;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id") 
    private KaraokeShop shop;
}
