package com.singeasy.booking_service.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.singeasy.booking_service.entity.Amenity;
import com.singeasy.booking_service.entity.Booking;
import com.singeasy.booking_service.entity.KaraokeShop;
import com.singeasy.booking_service.entity.Label;
import com.singeasy.booking_service.entity.Review;
import com.singeasy.booking_service.entity.Room;
import com.singeasy.booking_service.entity.User;
import com.singeasy.booking_service.enums.BookingStatusEnum;
import com.singeasy.booking_service.enums.RoleEnum;
import com.singeasy.booking_service.enums.RoomStatus;
import com.singeasy.booking_service.enums.ShopStatus;

final class RepositoryTestSupport {

    private RepositoryTestSupport() {
    }

    static User user(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword("password");
        user.setRole(RoleEnum.USER);
        return user;
    }

    static KaraokeShop shop(String name, String city, ShopStatus status) {
        KaraokeShop shop = new KaraokeShop();
        shop.setName(name);
        shop.setAddress("123 Street");
        shop.setCity(city);
        shop.setPhoneNumber("0900000000");
        shop.setMinPricePerHour(100.0);
        shop.setRating(4.5);
        shop.setStatus(status);
        return shop;
    }

    static Room room(KaraokeShop shop, String name, String capacity) {
        Room room = new Room();
        room.setName(name);
        room.setCapacity(capacity);
        room.setPricePerHour(150.0);
        room.setStatus(RoomStatus.AVAILABLE.name());
        room.setImageUrl("http://image.url/room.png");
        room.setShop(shop);
        return room;
    }

    static Booking booking(Room room, User user, LocalDate date, LocalTime startTime,
            int duration, BookingStatusEnum status, double totalAmount) {
        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setUser(user);
        booking.setBookingDate(date);
        booking.setStartTime(startTime);
        booking.setDuration(duration);
        booking.setPricePerHour(room.getPricePerHour());
        booking.setServiceFee(0.0);
        booking.setTotalAmount(totalAmount);
        booking.setStatus(status);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setShopId(room.getShop().getId());
        booking.setShopName(room.getShop().getName());
        booking.setRoomName(room.getName());
        return booking;
    }

    static Review review(KaraokeShop shop, User user, int rating, String comment) {
        Review review = new Review();
        review.setKaraokeShop(shop);
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now());
        return review;
    }

    static Amenity amenity(String name) {
        Amenity amenity = new Amenity();
        amenity.setName(name);
        return amenity;
    }

    static Label label(String name) {
        Label label = new Label();
        label.setName(name);
        return label;
    }
}
