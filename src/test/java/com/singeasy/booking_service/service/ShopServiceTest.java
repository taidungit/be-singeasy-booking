package com.singeasy.booking_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.singeasy.booking_service.dto.req.ShopReqDto;
import com.singeasy.booking_service.dto.res.ShopResDto;
import com.singeasy.booking_service.entity.Amenity;
import com.singeasy.booking_service.entity.KaraokeShop;
import com.singeasy.booking_service.entity.Label;
import com.singeasy.booking_service.enums.ShopStatus;
import com.singeasy.booking_service.repository.AmenityRepository;
import com.singeasy.booking_service.repository.LabelRepository;
import com.singeasy.booking_service.repository.ShopRepository;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private AmenityRepository amenityRepository;

    @Mock
    private LabelRepository labelRepository;

    @InjectMocks
    private ShopService shopService;

    @Test
    void findShops_returnsActiveShops() {
        KaraokeShop shop = buildShop(1L, "Shop A");
        ShopResDto dto = new ShopResDto();
        dto.setId(1L);
        dto.setName("Shop A");

        when(shopRepository.findByStatusNotOrderByIdAsc(ShopStatus.DELETED)).thenReturn(List.of(shop));
        when(modelMapper.map(shop, ShopResDto.class)).thenReturn(dto);

        List<ShopResDto> result = shopService.findShops();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Shop A");
    }

    @Test
    void getById_returnsShop_whenFound() {
        KaraokeShop shop = buildShop(1L, "Detail Shop");
        ShopResDto dto = new ShopResDto();
        dto.setName("Detail Shop");

        when(shopRepository.findByIdAndStatusNot(1L, ShopStatus.DELETED)).thenReturn(Optional.of(shop));
        when(modelMapper.map(shop, ShopResDto.class)).thenReturn(dto);

        ShopResDto result = shopService.getById(1L);

        assertThat(result.getName()).isEqualTo("Detail Shop");
    }

    @Test
    void getById_throws_whenNotFound() {
        when(shopRepository.findByIdAndStatusNot(99L, ShopStatus.DELETED)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shopService.getById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Shop not found");
    }

    @Test
    void createShop_savesShopWithAmenitiesAndLabels() {
        ShopReqDto request = new ShopReqDto();
        request.setName("New Shop");
        request.setAmenities(List.of("Wifi"));
        request.setLabels(List.of("Popular"));

        KaraokeShop mappedShop = new KaraokeShop();
        mappedShop.setName("New Shop");
        mappedShop.setAmenities(new ArrayList<>());
        mappedShop.setLabels(new ArrayList<>());

        Amenity wifi = new Amenity();
        wifi.setName("Wifi");
        Label popular = new Label();
        popular.setName("Popular");

        KaraokeShop savedShop = buildShop(10L, "New Shop");
        savedShop.setAmenities(List.of(wifi));
        savedShop.setLabels(List.of(popular));

        ShopResDto response = new ShopResDto();
        response.setId(10L);
        response.setName("New Shop");

        when(modelMapper.map(request, KaraokeShop.class)).thenReturn(mappedShop);
        when(amenityRepository.findByName("Wifi")).thenReturn(Optional.of(wifi));
        when(labelRepository.findByName("Popular")).thenReturn(Optional.of(popular));
        when(shopRepository.save(mappedShop)).thenReturn(savedShop);
        when(modelMapper.map(savedShop, ShopResDto.class)).thenReturn(response);

        ShopResDto result = shopService.createShop(request);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getAmenities()).containsExactly("Wifi");
        assertThat(result.getLabels()).containsExactly("Popular");
    }

    @Test
    void deleteShop_setsStatusToDeleted() {
        KaraokeShop shop = buildShop(5L, "To Delete");
        when(shopRepository.findById(5L)).thenReturn(Optional.of(shop));
        when(shopRepository.save(shop)).thenReturn(shop);

        shopService.deleteShop(5L);

        assertThat(shop.getStatus()).isEqualTo(ShopStatus.DELETED);
        verify(shopRepository).save(shop);
    }

    @Test
    void getDistinctCities_returnsCities() {
        when(shopRepository.findDistinctCities()).thenReturn(List.of("Hanoi", "Da Nang"));

        assertThat(shopService.getDistinctCities()).containsExactly("Hanoi", "Da Nang");
    }

    @Test
    void filterShops_normalizesAllAddressToNull() {
        KaraokeShop shop = buildShop(1L, "Filtered");
        ShopResDto dto = new ShopResDto();
        dto.setName("Filtered");

        when(shopRepository.findFilteredShopsNative("Karaoke", null, 4.0, 100, 500))
                .thenReturn(List.of(shop));
        when(modelMapper.map(shop, ShopResDto.class)).thenReturn(dto);

        List<ShopResDto> result = shopService.filterShops("Karaoke", "all", 4.0, 100, 500);

        assertThat(result).hasSize(1);
        verify(shopRepository).findFilteredShopsNative(eq("Karaoke"), eq(null), eq(4.0), eq(100), eq(500));
    }

    private KaraokeShop buildShop(Long id, String name) {
        KaraokeShop shop = new KaraokeShop();
        shop.setId(id);
        shop.setName(name);
        shop.setStatus(ShopStatus.ACTIVE);
        shop.setAmenities(new ArrayList<>());
        shop.setLabels(new ArrayList<>());
        return shop;
    }
}
