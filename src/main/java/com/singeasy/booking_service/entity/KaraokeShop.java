package com.singeasy.booking_service.entity;


import java.util.ArrayList;
import java.util.List;
import com.singeasy.booking_service.enums.ShopStatus;
import jakarta.persistence.CascadeType;
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
    private Double minPricePerHour;


    @Lob // Đánh dấu đây là đối tượng lớn
    @Column(name = "image_url", columnDefinition = "LONGTEXT")
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    private ShopStatus status = ShopStatus.ACTIVE;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
        name = "shop_label",
        joinColumns = @JoinColumn(name = "shop_id"),
        inverseJoinColumns = @JoinColumn(name = "label_id")
    )
    private List<Label> labels = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "shop_amenity",
        joinColumns = @JoinColumn(name = "shop_id"),
        inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private List<Amenity> amenities;

    @OneToMany(mappedBy = "shop")
    private List<Room> rooms;

    private Double rating=0.0;


    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @OneToMany(mappedBy = "karaokeShop", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();

    public void recalculateRating() {
        if (this.reviews == null || this.reviews.isEmpty()) {
            this.rating = 0.0;
            this.reviewCount = 0;
            return;
        }
        
        this.reviewCount = this.reviews.size();
        
        double totalStars = 0;
        for (Review r : this.reviews) {
            totalStars += r.getRating();
        }
        
        this.rating = Math.round((totalStars / this.reviewCount) * 10.0) / 10.0;
    }
}