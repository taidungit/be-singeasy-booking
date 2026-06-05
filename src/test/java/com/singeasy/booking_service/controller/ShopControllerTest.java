package com.singeasy.booking_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.singeasy.booking_service.dto.req.ShopReqDto;
import com.singeasy.booking_service.dto.res.ShopResDto;
import com.singeasy.booking_service.service.ShopService;

@WebMvcTest(ShopController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShopControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShopService shopService;

    @Test
    void getAllShops_returnsOk() throws Exception {
        ShopResDto shop = new ShopResDto();
        shop.setId(1L);
        shop.setName("Karaoke A");
        when(shopService.findShops()).thenReturn(List.of(shop));

        mockMvc.perform(get("/api/v1/shops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Karaoke A"));
    }

    @Test
    void searchShops_returnsFilteredResults() throws Exception {
        ShopResDto shop = new ShopResDto();
        shop.setId(2L);
        shop.setName("Karaoke B");
        when(shopService.filterShops("Karaoke", "Hanoi", 4.0, 100, 500))
                .thenReturn(List.of(shop));

        mockMvc.perform(get("/api/v1/shops/search")
                        .param("name", "Karaoke")
                        .param("address", "Hanoi")
                        .param("minRating", "4.0")
                        .param("minPrice", "100")
                        .param("maxPrice", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Karaoke B"));
    }

    @Test
    void getShopDetail_returnsShop() throws Exception {
        ShopResDto shop = new ShopResDto();
        shop.setId(1L);
        shop.setName("Detail Shop");
        when(shopService.getById(1L)).thenReturn(shop);

        mockMvc.perform(get("/api/v1/shops/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Detail Shop"));
    }

    @Test
    void createShop_returnsCreated() throws Exception {
        ShopReqDto request = validShopRequest();
        ShopResDto response = new ShopResDto();
        response.setId(10L);
        response.setName(request.getName());
        when(shopService.createShop(any(ShopReqDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/shops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("New Shop"));
    }

    @Test
    void updateShop_returnsOk() throws Exception {
        ShopReqDto request = validShopRequest();
        ShopResDto response = new ShopResDto();
        response.setId(1L);
        response.setName("Updated Shop");
        when(shopService.updateShop(eq(1L), any(ShopReqDto.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/shops/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Shop"));
    }

    @Test
    void deleteShop_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/v1/shops/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Deleted shop with id: 5"));

        verify(shopService).deleteShop(5L);
    }

    private ShopReqDto validShopRequest() {
        ShopReqDto dto = new ShopReqDto();
        dto.setName("New Shop");
        dto.setCity("Hanoi");
        dto.setPhoneNumber("0900000000");
        dto.setAddress("123 Street");
        dto.setMinPricePerHour(100.0);
        return dto;
    }
}
