package com.singeasy.booking_service.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.singeasy.booking_service.dto.shop.ShopUpdateDto;
import com.singeasy.booking_service.entity.Amenity;
import com.singeasy.booking_service.entity.KaraokeShop;
import com.singeasy.booking_service.entity.Label;
import com.singeasy.booking_service.enums.ShopStatus;
import com.singeasy.booking_service.repository.ShopRepository;

@Service
    public class ShopService {
        private final ShopRepository shopRepository;
        public ShopService(ShopRepository shopRepository) {
            this.shopRepository = shopRepository;
        }
        public List<KaraokeShop> findShops() {
            return shopRepository.findByStatusNot(ShopStatus.DELETED);
        }
        public KaraokeShop getById(Long id) {
            return shopRepository.findByIdAndStatusNot(id, ShopStatus.DELETED)
                    .orElseThrow(() -> new RuntimeException("Shop not found"));
        }

        public KaraokeShop createShop(ShopUpdateDto dto) {
        KaraokeShop shop = new KaraokeShop();
        updateEntityFromDto(shop, dto);
        return shopRepository.save(shop);
    }

    public KaraokeShop updateShop(Long id, ShopUpdateDto dto) {
        KaraokeShop shop = shopRepository.findByIdAndStatusNot(id, ShopStatus.DELETED)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        // Gọi hàm map chung
        updateEntityFromDto(shop, dto);
        return shopRepository.save(shop);
    }

    // Hàm bổ trợ để dùng chung logic
    private void updateEntityFromDto(KaraokeShop shop, ShopUpdateDto dto) {
        shop.setName(dto.getName());
        shop.setCity(dto.getCity());
        shop.setPhoneNumber(dto.getPhoneNumber());
        shop.setAddress(dto.getAddress());
        shop.setOpeningHours(dto.getOpeningHours());
        shop.setMinPricePerHour(dto.getMinPricePerHour());
        shop.setImageUrl(dto.getImageUrl());
        shop.setDescription(dto.getDescription());

        // Xử lý Labels
        if (dto.getLabels() != null) {
            if (shop.getLabels() == null) shop.setLabels(new ArrayList<>());
            shop.getLabels().clear();
            dto.getLabels().forEach(name -> {
                Label label = new Label();
                label.setName(name);
                label.setShop(shop);
                shop.getLabels().add(label);
            });
        }

        // Xử lý Amenities
        if (dto.getAmenities() != null) {
            if (shop.getAmenities() == null) shop.setAmenities(new ArrayList<>());
            shop.getAmenities().clear();
            dto.getAmenities().forEach(name -> {
                Amenity amenity = new Amenity();
                amenity.setName(name);
                amenity.setShop(shop);
                shop.getAmenities().add(amenity);
            });
        }
    }

    public void deleteShop(Long id) {
        KaraokeShop shop = getById(id);
        shop.setStatus(ShopStatus.DELETED);
        shopRepository.save(shop);
    }
}
