package com.singeasy.booking_service.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class KaraokeShopTest {

    @Test
    void recalculateRating_resetsToZero_whenReviewsEmpty() {
        KaraokeShop shop = new KaraokeShop();
        shop.setRating(4.5);
        shop.setReviewCount(3);
        shop.setReviews(new ArrayList<>());

        shop.recalculateRating();

        assertThat(shop.getRating()).isEqualTo(0.0);
        assertThat(shop.getReviewCount()).isEqualTo(0);
    }

    @Test
    void recalculateRating_resetsToZero_whenReviewsNull() {
        KaraokeShop shop = new KaraokeShop();
        shop.setRating(4.5);
        shop.setReviewCount(3);
        shop.setReviews(null);

        shop.recalculateRating();

        assertThat(shop.getRating()).isEqualTo(0.0);
        assertThat(shop.getReviewCount()).isEqualTo(0);
    }

    @Test
    void recalculateRating_computesAverageRoundedToOneDecimal() {
        KaraokeShop shop = new KaraokeShop();
        shop.setReviews(List.of(
                reviewWithRating(5),
                reviewWithRating(4),
                reviewWithRating(4)));

        shop.recalculateRating();

        assertThat(shop.getReviewCount()).isEqualTo(3);
        assertThat(shop.getRating()).isEqualTo(4.3);
    }

    @Test
    void recalculateRating_handlesSingleReview() {
        KaraokeShop shop = new KaraokeShop();
        shop.setReviews(List.of(reviewWithRating(5)));

        shop.recalculateRating();

        assertThat(shop.getReviewCount()).isEqualTo(1);
        assertThat(shop.getRating()).isEqualTo(5.0);
    }

    private Review reviewWithRating(int rating) {
        Review review = new Review();
        review.setRating(rating);
        return review;
    }
}
