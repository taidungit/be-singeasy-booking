package com.singeasy.booking_service.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.singeasy.booking_service.dto.req.ReviewReqDto;
import com.singeasy.booking_service.dto.res.ReviewResDto;
import com.singeasy.booking_service.service.ReviewService;
import com.singeasy.booking_service.util.SecurityUtil; // Hoặc class lấy thông tin security tương tự của bạn

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResDto> createReview(@Valid @RequestBody ReviewReqDto reviewReqDto) {
        // 1. Bốc Email của User đang đăng nhập từ hệ thống Security Token ngầm (Né truyền userId từ FE lên)
        String currentUserEmail = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("Unauthorized: Please login to submit a review"));

        // 2. Gọi xuống tầng Service xử lý lưu và tính toán số sao
        ReviewResDto savedReview = reviewService.createReview(reviewReqDto, currentUserEmail);
        
        // 3. Trả về status 201 Created kèm dữ liệu review vừa tạo thành công
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReview);
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<ReviewResDto>> getReviewsByShop(@PathVariable Long shopId) {
        List<ReviewResDto> reviews = reviewService.getReviewsByShop(shopId);
        return ResponseEntity.ok(reviews);
    }
}