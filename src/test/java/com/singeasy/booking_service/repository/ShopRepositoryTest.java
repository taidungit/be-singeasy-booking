package com.singeasy.booking_service.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.singeasy.booking_service.enums.ShopStatus;

@DataJpaTest
class ShopRepositoryTest {

    @Autowired
    private ShopRepository shopRepository;

    @Test
    void findByStatusNot_excludesDeletedShops() {
        shopRepository.save(RepositoryTestSupport.shop("Active Shop", "Hanoi", ShopStatus.ACTIVE));
        shopRepository.save(RepositoryTestSupport.shop("Deleted Shop", "Hanoi", ShopStatus.DELETED));

        assertThat(shopRepository.findByStatusNotOrderByIdAsc(ShopStatus.DELETED))
                .hasSize(1)
                .first()
                .extracting("name")
                .isEqualTo("Active Shop");
    }

    @Test
    void findByIdAndStatusNot_returnsShop_whenNotDeleted() {
        var saved = shopRepository.save(
                RepositoryTestSupport.shop("Detail Shop", "Da Nang", ShopStatus.ACTIVE));

        assertThat(shopRepository.findByIdAndStatusNot(saved.getId(), ShopStatus.DELETED))
                .isPresent()
                .get()
                .extracting("name")
                .isEqualTo("Detail Shop");
    }

    @Test
    void findByIdAndStatusNot_returnsEmpty_whenDeleted() {
        var saved = shopRepository.save(
                RepositoryTestSupport.shop("Gone Shop", "Hue", ShopStatus.DELETED));

        assertThat(shopRepository.findByIdAndStatusNot(saved.getId(), ShopStatus.DELETED)).isEmpty();
    }

    @Test
    void findDistinctCities_returnsUniqueCities() {
        shopRepository.save(RepositoryTestSupport.shop("Shop A", "Hanoi", ShopStatus.ACTIVE));
        shopRepository.save(RepositoryTestSupport.shop("Shop B", "Hanoi", ShopStatus.ACTIVE));
        shopRepository.save(RepositoryTestSupport.shop("Shop C", "Da Nang", ShopStatus.ACTIVE));

        assertThat(shopRepository.findDistinctCities())
                .containsExactlyInAnyOrder("Hanoi", "Da Nang");
    }

    @Test
    void findFilteredShopsNative_filtersByNameAndPrice() {
        var cheap = RepositoryTestSupport.shop("Budget Karaoke", "Hanoi", ShopStatus.ACTIVE);
        cheap.setMinPricePerHour(80.0);
        cheap.setRating(3.5);

        var premium = RepositoryTestSupport.shop("Premium Karaoke", "Hanoi", ShopStatus.ACTIVE);
        premium.setMinPricePerHour(300.0);
        premium.setRating(4.8);

        shopRepository.save(cheap);
        shopRepository.save(premium);

        assertThat(shopRepository.findFilteredShopsNative("Budget", null, null, null, null))
                .hasSize(1)
                .first()
                .extracting("name")
                .isEqualTo("Budget Karaoke");

        assertThat(shopRepository.findFilteredShopsNative(null, null, 4.0, 200, 500))
                .hasSize(1)
                .first()
                .extracting("name")
                .isEqualTo("Premium Karaoke");
    }
}
