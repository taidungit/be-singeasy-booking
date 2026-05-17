package com.singeasy.booking_service.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.singeasy.booking_service.dto.req.ReviewReqDto;
import com.singeasy.booking_service.dto.res.ReviewResDto;
import com.singeasy.booking_service.entity.Review;
import com.singeasy.booking_service.entity.KaraokeShop;
import com.singeasy.booking_service.entity.User;
import com.singeasy.booking_service.repository.ReviewRepository;
import com.singeasy.booking_service.repository.ShopRepository;
import com.singeasy.booking_service.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ShopRepository karaokeShopRepository;
    private final UserRepository userRepository;


    public ReviewResDto convertToReviewResDto(Review review) {
        if (review == null) return null;
        
        ReviewResDto res = new ReviewResDto();
        res.setId(review.getId());
        res.setRating(review.getRating());
        res.setComment(review.getComment());
        res.setCreatedAt(review.getCreatedAt());
        
        // Bốc thông tin người viết từ liên kết User Entity sang
        if (review.getUser() != null) {
            res.setUserName(review.getUser().getName());
            res.setUserEmail(review.getUser().getEmail());
        }
        return res;
    }


    @Transactional
    public ReviewResDto createReview(ReviewReqDto dto, String currentUserEmail) {
        KaraokeShop shop = karaokeShopRepository.findById(dto.getShopId())
                .orElseThrow(() -> new RuntimeException("Karaoke Shop not found with ID: " + dto.getShopId()));

        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        Review review = new Review();
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setKaraokeShop(shop); 
        review.setUser(user);        

        Review savedReview = reviewRepository.save(review);
        shop.getReviews().add(savedReview);
        shop.recalculateRating();
        karaokeShopRepository.save(shop);

        return convertToReviewResDto(savedReview);
    }

    @Transactional(readOnly = true)
    public List<ReviewResDto> getReviewsByShop(Long shopId) {
        // Kiểm tra xem quán có tồn tại hay không trước khi tìm kiếm
        if (!karaokeShopRepository.existsById(shopId)) {
            throw new RuntimeException("Karaoke Shop not found with ID: " + shopId);
        }

        // Truy vấn MySQL để bốc toàn bộ review có shop_id tương ứng
        List<Review> reviews = reviewRepository.findByKaraokeShopIdOrderByCreatedAtDesc(shopId);

        // Map mảng Entity sang mảng DTO sạch sẽ để đẩy về cho Frontend hiển thị
        return reviews.stream()
                .map(this::convertToReviewResDto)
                .collect(Collectors.toList());
    }
}