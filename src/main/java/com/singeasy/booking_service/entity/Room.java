package com.singeasy.booking_service.entity;


import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
    
    private String status;
    
    private String imageUrl;

    @ElementCollection
    private List<String> amenities;

    // Kết nối với Shop
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id") 
    private KaraokeShop shop;
}
