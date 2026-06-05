package com.singeasy.booking_service.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.singeasy.booking_service.enums.ShopStatus;

@DataJpaTest
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Test
    void findByShopId_returnsRoomsForShop() {
        var shopA = shopRepository.save(RepositoryTestSupport.shop("Shop A", "Hanoi", ShopStatus.ACTIVE));
        var shopB = shopRepository.save(RepositoryTestSupport.shop("Shop B", "Hue", ShopStatus.ACTIVE));

        roomRepository.save(RepositoryTestSupport.room(shopA, "VIP 1", "10"));
        roomRepository.save(RepositoryTestSupport.room(shopA, "VIP 2", "8"));
        roomRepository.save(RepositoryTestSupport.room(shopB, "Standard", "6"));

        assertThat(roomRepository.findByShopId(shopA.getId()))
                .hasSize(2)
                .extracting("name")
                .containsExactlyInAnyOrder("VIP 1", "VIP 2");
    }

    @Test
    void findDistinctCapacities_returnsUniqueCapacities() {
        var shop = shopRepository.save(RepositoryTestSupport.shop("Shop A", "Hanoi", ShopStatus.ACTIVE));

        roomRepository.save(RepositoryTestSupport.room(shop, "Room 1", "4"));
        roomRepository.save(RepositoryTestSupport.room(shop, "Room 2", "8"));
        roomRepository.save(RepositoryTestSupport.room(shop, "Room 3", "4"));

        assertThat(roomRepository.findDistinctCapacities())
                .containsExactlyInAnyOrder("4", "8");
    }
}
