package com.singeasy.booking_service.entity;


import java.util.List;
import com.singeasy.booking_service.enums.ShopStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class KaraokeShop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String address;
    private String city;
    private String phoneNumber;
    private String openingHours;
    private String description;
    private Double rating;
    private Integer reviewCount;
    private Double minPricePerHour;
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    private ShopStatus status = ShopStatus.ACTIVE;
    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    private List<Highlight> highlights;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Label> labels;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Amenity> amenities;

    @OneToMany(mappedBy = "shop")
    private List<Room> rooms;
}