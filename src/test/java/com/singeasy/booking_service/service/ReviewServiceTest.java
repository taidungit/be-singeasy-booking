package com.singeasy.booking_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.singeasy.booking_service.dto.req.ReviewReqDto;
import com.singeasy.booking_service.dto.res.ReviewResDto;
import com.singeasy.booking_service.entity.KaraokeShop;
import com.singeasy.booking_service.entity.Review;
import com.singeasy.booking_service.entity.User;
import com.singeasy.booking_service.repository.ReviewRepository;
import com.singeasy.booking_service.repository.ShopRepository;
import com.singeasy.booking_service.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ShopRepository karaokeShopRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void createReview_savesReviewAndRecalculatesRating() {
        ReviewReqDto request = new ReviewReqDto();
        request.setRating(5);
        request.setComment("Great place");
        request.setShopId(1L);

        KaraokeShop shop = new KaraokeShop();
        shop.setId(1L);
        shop.setReviews(new ArrayList<>());

        User user = new User();
        user.setId(2L);
        user.setName("Reviewer");
        user.setEmail("user@test.com");

        when(karaokeShopRepository.findById(1L)).thenReturn(Optional.of(shop));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(10L);
            review.setCreatedAt(LocalDateTime.now());
            review.setUser(user);
            return review;
        });
        when(karaokeShopRepository.save(shop)).thenReturn(shop);

        ReviewResDto result = reviewService.createReview(request, "user@test.com");

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getUserEmail()).isEqualTo("user@test.com");
        assertThat(shop.getRating()).isEqualTo(5.0);
        assertThat(shop.getReviewCount()).isEqualTo(1);
        verify(karaokeShopRepository).save(shop);
    }

    @Test
    void createReview_throws_whenShopNotFound() {
        ReviewReqDto request = new ReviewReqDto();
        request.setShopId(99L);
        request.setRating(5);
        request.setComment("Nice");

        when(karaokeShopRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(request, "user@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Karaoke Shop not found with ID: 99");
    }

    @Test
    void getReviewsByShop_returnsReviews() {
        User user = new User();
        user.setName("Reviewer");
        user.setEmail("user@test.com");

        Review review = new Review();
        review.setId(1L);
        review.setRating(4);
        review.setComment("Good");
        review.setCreatedAt(LocalDateTime.now());
        review.setUser(user);

        when(karaokeShopRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.findByKaraokeShopIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(review));

        List<ReviewResDto> result = reviewService.getReviewsByShop(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRating()).isEqualTo(4);
        assertThat(result.get(0).getUserName()).isEqualTo("Reviewer");
    }

    @Test
    void getReviewsByShop_throws_whenShopNotFound() {
        when(karaokeShopRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.getReviewsByShop(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Karaoke Shop not found with ID: 99");
    }

    @Test
    void convertToReviewResDto_returnsNull_whenReviewIsNull() {
        assertThat(reviewService.convertToReviewResDto(null)).isNull();
    }
}
