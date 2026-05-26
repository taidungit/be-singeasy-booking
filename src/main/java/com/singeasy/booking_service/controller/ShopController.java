package com.singeasy.booking_service.controller;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.singeasy.booking_service.dto.req.ShopReqDto;
import com.singeasy.booking_service.dto.res.ShopResDto;
import com.singeasy.booking_service.service.ShopService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/shops")
public class ShopController {

    @Autowired
    private ShopService shopService;

    @GetMapping
    public ResponseEntity<List<ShopResDto>> getAllShops() {
        return ResponseEntity.ok(shopService.findShops());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ShopResDto>> searchShops(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice) {
            
        return ResponseEntity.ok(shopService.filterShops(name, address, minRating, minPrice, maxPrice));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShopResDto> getShopDetail(@PathVariable Long id) { // Thêm @PathVariable bị thiếu
        return ResponseEntity.ok(shopService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ShopResDto> create(@Valid @RequestBody ShopReqDto shop) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shopService.createShop(shop));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShopResDto> update(@PathVariable Long id, @Valid @RequestBody ShopReqDto shopDetails) {
        return ResponseEntity.ok(shopService.updateShop(id, shopDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        shopService.deleteShop(id);
        return ResponseEntity.ok("Deleted shop with id: " + id);
    }
}