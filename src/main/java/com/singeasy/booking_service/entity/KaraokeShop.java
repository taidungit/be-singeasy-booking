package com.singeasy.booking_service.entity;


import java.util.List;
import com.singeasy.booking_service.enums.ShopStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
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

    @Lob // Đánh dấu đây là đối tượng lớn
    @Column(name = "image_url", columnDefinition = "LONGTEXT")
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    private ShopStatus status = ShopStatus.ACTIVE;
    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    private List<Highlight> highlights;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Label> labels;

    @ManyToMany
    @JoinTable(
        name = "shop_amenity",
        joinColumns = @JoinColumn(name = "shop_id"),
        inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private List<Amenity> amenities;

    @OneToMany(mappedBy = "shop")
    private List<Room> rooms;
}