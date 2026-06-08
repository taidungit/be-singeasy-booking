package com.singeasy.booking_service.service;

import java.util.ArrayList;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import com.singeasy.booking_service.dto.req.ShopReqDto;
import com.singeasy.booking_service.dto.res.ShopResDto;
import com.singeasy.booking_service.entity.Amenity;
import com.singeasy.booking_service.entity.KaraokeShop;
import com.singeasy.booking_service.entity.Label;
import com.singeasy.booking_service.enums.ShopStatus;
import com.singeasy.booking_service.repository.AmenityRepository;
import com.singeasy.booking_service.repository.LabelRepository;
import com.singeasy.booking_service.repository.ShopRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class ShopService {
    private final ShopRepository shopRepository;
    private final ModelMapper modelMapper;
    private final AmenityRepository amenityRepository;
    private final LabelRepository labelRepository;

    public ShopService(ShopRepository shopRepository, ModelMapper modelMapper, AmenityRepository amenityRepository, LabelRepository labelRepository) {
        this.shopRepository = shopRepository;
        this.modelMapper = modelMapper;
        this.amenityRepository = amenityRepository;
        this.labelRepository = labelRepository;
    }

    public List<ShopResDto> findShops() {
        return shopRepository.findByStatusNotOrderByIdAsc(ShopStatus.DELETED)
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
    // 1. Map cơ bản
    KaraokeShop shop = modelMapper.map(dto, KaraokeShop.class);
    
    // 2. Xử lý Labels (Giống hệt Amenity - Bắt buộc phải có sẵn trong DB)
    if (dto.getLabels() != null && !dto.getLabels().isEmpty()) {
        List<Label> existingLabels = dto.getLabels().stream()
            .map(name -> labelRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Label not found: " + name)))
            .toList();
        shop.setLabels(existingLabels);
    } else {
        shop.setLabels(new ArrayList<>());
    }
    
    // 3. Xử lý Amenities
    if (dto.getAmenities() != null && !dto.getAmenities().isEmpty()) {
        List<Amenity> existingAmenities = dto.getAmenities().stream()
            .map(name -> amenityRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Amenity not found: " + name)))
            .toList();
        shop.setAmenities(existingAmenities);
    } else {
        shop.setAmenities(new ArrayList<>());
    }
    
    return convertToResDto(shopRepository.save(shop));
}

@Transactional
public ShopResDto updateShop(Long id, ShopReqDto dto) {
    KaraokeShop existingShop = shopRepository.findByIdAndStatusNot(id, ShopStatus.DELETED)
            .orElseThrow(() -> new RuntimeException("Shop not found"));
            
    modelMapper.map(dto, existingShop);
    
    // 2. Cập nhật Labels (Giống hệt Amenity - Chỉ thay đổi liên kết)
    if (dto.getLabels() != null) {
        existingShop.getLabels().clear(); 
        dto.getLabels().forEach(name -> {
            Label label = labelRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Label not found: " + name));
            existingShop.getLabels().add(label);
        });
    }
    
    // 3. Cập nhật Amenities
    if (dto.getAmenities() != null) {
        existingShop.getAmenities().clear(); 
        dto.getAmenities().forEach(name -> {
            Amenity amenity = amenityRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Amenity not found: " + name));
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

    public List<String> getDistinctCities() {
        return shopRepository.findDistinctCities();
    }

    public List<ShopResDto> filterShops(String name, String address, Double minRating, Integer minPrice, Integer maxPrice) {
        // Xử lý chuẩn hóa từ 'all' của Frontend thành null để câu SQL Native bỏ qua điều kiện lọc địa chỉ
        String filterAddress = (address != null && address.equalsIgnoreCase("all")) ? null : address;

        return shopRepository.findFilteredShopsNative(name, filterAddress, minRating, minPrice, maxPrice)
                .stream()
                .map(this::convertToResDto)
                .toList();
    }
}