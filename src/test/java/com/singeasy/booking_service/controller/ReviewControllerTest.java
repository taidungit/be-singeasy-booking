package com.singeasy.booking_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.singeasy.booking_service.dto.req.ReviewReqDto;
import com.singeasy.booking_service.dto.res.ReviewResDto;
import com.singeasy.booking_service.service.ReviewService;
import com.singeasy.booking_service.util.SecurityUtil;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    @Test
    void createReview_returnsCreated() throws Exception {
        ReviewReqDto request = new ReviewReqDto();
        request.setRating(5);
        request.setComment("Great place");
        request.setShopId(1L);

        ReviewResDto response = new ReviewResDto();
        response.setId(1L);
        response.setRating(5);
        response.setComment("Great place");
        response.setCreatedAt(LocalDateTime.now());
        response.setUserEmail("user@test.com");

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserLogin).thenReturn(Optional.of("user@test.com"));
            when(reviewService.createReview(any(ReviewReqDto.class), eq("user@test.com"))).thenReturn(response);

            mockMvc.perform(post("/api/v1/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.rating").value(5))
                    .andExpect(jsonPath("$.comment").value("Great place"));
        }
    }

    @Test
    void getReviewsByShop_returnsOk() throws Exception {
        ReviewResDto review = new ReviewResDto();
        review.setId(1L);
        review.setRating(4);
        review.setComment("Nice");
        when(reviewService.getReviewsByShop(1L)).thenReturn(List.of(review));

        mockMvc.perform(get("/api/v1/reviews/shop/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rating").value(4));
    }
}
