package com.singeasy.booking_service.controller;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.singeasy.booking_service.dto.shop.ShopUpdateDto;
import com.singeasy.booking_service.entity.KaraokeShop;
import com.singeasy.booking_service.service.ShopService;

@RestController
@RequestMapping("/api/shops")
public class ShopController {

    @Autowired
    private ShopService shopService;

    @GetMapping
    public ResponseEntity<List<KaraokeShop>> getAllShops(){
        return ResponseEntity.ok(shopService.findShops());
    }

    @GetMapping("/{id}")
    public ResponseEntity<KaraokeShop> getShopDetail(Long id) {
        return ResponseEntity.ok(shopService.getById(id));
    }

    @PostMapping
    public KaraokeShop create(@RequestBody ShopUpdateDto shop) {
        return shopService.createShop(shop);
    }

    @PutMapping("/{id}")
    public ResponseEntity<KaraokeShop> update(@PathVariable Long id, @RequestBody ShopUpdateDto shopDetails) {
        return ResponseEntity.ok(shopService.updateShop(id, shopDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        shopService.deleteShop(id);
        return ResponseEntity.ok("Deleted shop with id: " + id);
    }
}
