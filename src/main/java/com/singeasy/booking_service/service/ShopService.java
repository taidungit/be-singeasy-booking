package com.singeasy.booking_service.service;

import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import com.singeasy.booking_service.dto.req.ShopReqDto;
import com.singeasy.booking_service.dto.res.ShopResDto;
import com.singeasy.booking_service.entity.Amenity;
import com.singeasy.booking_service.entity.KaraokeShop;
import com.singeasy.booking_service.entity.Label;
import com.singeasy.booking_service.enums.ShopStatus;
import com.singeasy.booking_service.repository.ShopRepository;

import jakarta.transaction.Transactional;

@Service
public class ShopService {
    private final ShopRepository shopRepository;
    private final ModelMapper modelMapper;

    public ShopService(ShopRepository shopRepository, ModelMapper modelMapper) {
        this.shopRepository = shopRepository;
        this.modelMapper = modelMapper;
    }

    public List<ShopResDto> findShops() {
        return shopRepository.findByStatusNot(ShopStatus.DELETED)
                .stream()
                .map(this::convertToResDto)
                .toList();
    }

    public ShopResDto getById(Long id) {
        KaraokeShop shop = shopRepository.findByIdAndStatusNot(id, ShopStatus.DELETED)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        return convertToResDto(shop);
    }

    @Transactional
    public ShopResDto createShop(ShopReqDto dto) {
        KaraokeShop shop = modelMapper.map(dto, KaraokeShop.class);
        
        if (dto.getLabels() != null) {
            shop.setLabels(dto.getLabels().stream().map(name -> {
                Label label = new Label();
                label.setName(name);
                label.setShop(shop);
                return label;
            }).toList());
        }
        if (dto.getAmenities() != null) {
            shop.setAmenities(dto.getAmenities().stream().map(name -> {
                Amenity amenity = new Amenity();
                amenity.setName(name);
                amenity.setShop(shop);
                return amenity;
            }).toList());
        }
        
        return convertToResDto(shopRepository.save(shop));
    }

@Transactional
public ShopResDto updateShop(Long id, ShopReqDto dto) {
    KaraokeShop existingShop = shopRepository.findByIdAndStatusNot(id, ShopStatus.DELETED)
            .orElseThrow(() -> new RuntimeException("Shop not found"));

    modelMapper.map(dto, existingShop);
    if (dto.getLabels() != null) {
        existingShop.getLabels().clear(); 
        
        dto.getLabels().forEach(name -> {
            Label label = new Label();
            label.setName(name);
            label.setShop(existingShop);
            existingShop.getLabels().add(label); 
        });
    }

    if (dto.getAmenities() != null) {
        existingShop.getAmenities().clear(); 
        
        dto.getAmenities().forEach(name -> {
            Amenity amenity = new Amenity();
            amenity.setName(name);
            amenity.setShop(existingShop);
            existingShop.getAmenities().add(amenity);
        });
    }

    return convertToResDto(shopRepository.save(existingShop));
}

    public void deleteShop(Long id) {
        KaraokeShop shop = shopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        shop.setStatus(ShopStatus.DELETED);
        shopRepository.save(shop);
    }


    private ShopResDto convertToResDto(KaraokeShop shop) {
        ShopResDto res = modelMapper.map(shop, ShopResDto.class);
        
        if (shop.getLabels() != null) {
            res.setLabels(shop.getLabels().stream().map(Label::getName).toList());
        }
        if (shop.getAmenities() != null) {
            res.setAmenities(shop.getAmenities().stream().map(Amenity::getName).toList());
        }
        
        return res;
    }
}