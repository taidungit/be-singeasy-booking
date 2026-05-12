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
import com.singeasy.booking_service.repository.ShopRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class ShopService {
    private final ShopRepository shopRepository;
    private final ModelMapper modelMapper;
    private final AmenityRepository amenityRepository;

    public ShopService(ShopRepository shopRepository, ModelMapper modelMapper, AmenityRepository amenityRepository) {
        this.shopRepository = shopRepository;
        this.modelMapper = modelMapper;
        this.amenityRepository = amenityRepository;
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
    // 1. Map cơ bản
    KaraokeShop shop = modelMapper.map(dto, KaraokeShop.class);
    
    // 2. Xử lý Labels (Nếu có thì mới làm, không thì thôi)
    if (dto.getLabels() != null && !dto.getLabels().isEmpty()) {
        shop.setLabels(dto.getLabels().stream().map(name -> {
            Label label = new Label();
            label.setName(name);
            label.setShop(shop);
            return label;
        }).toList());
    }
    // 3. Xử lý Amenities (Chỉ xử lý khi dto.getAmenities() có dữ liệu)
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
    // 1. Map các trường cơ bản từ DTO sang Entity hiện tại
    modelMapper.map(dto, existingShop);
    // 2. Cập nhật Labels (Quan hệ OneToMany - tạo mới/xóa bỏ bình thường)
    if (dto.getLabels() != null) {
        existingShop.getLabels().clear(); 
        dto.getLabels().forEach(name -> {
            Label label = new Label();
            label.setName(name);
            label.setShop(existingShop);
            existingShop.getLabels().add(label); 
        });
    }
    // 3. Cập nhật Amenities (Quan hệ ManyToMany - Chỉ thay đổi liên kết)
    if (dto.getAmenities() != null) {
        // Xóa các liên kết cũ trong bảng trung gian (shop_amenity)
        existingShop.getAmenities().clear(); 
        // Tìm các Amenity "cứng" từ DB và thêm vào danh sách liên kết mới
        dto.getAmenities().forEach(name -> {
            Amenity amenity = amenityRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Amenity not found: " + name));
            // Chỉ thêm liên kết, KHÔNG setShop hay tạo mới Amenity
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