package com.singeasy.booking_service.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.singeasy.booking_service.dto.req.ShopReqDto;
import com.singeasy.booking_service.entity.KaraokeShop;

@Configuration
public class ApplicationConfig {

@Bean
public ModelMapper modelMapper() {
    ModelMapper modelMapper = new ModelMapper();
    
    modelMapper.typeMap(ShopReqDto.class, KaraokeShop.class).addMappings(mapper -> {
        mapper.skip(KaraokeShop::setAmenities);
        mapper.skip(KaraokeShop::setLabels);
    });
    
    return modelMapper;
}
}