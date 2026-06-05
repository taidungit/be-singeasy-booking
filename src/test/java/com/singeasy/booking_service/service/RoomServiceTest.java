package com.singeasy.booking_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.singeasy.booking_service.dto.req.RoomReqDto;
import com.singeasy.booking_service.dto.res.RoomResDto;
import com.singeasy.booking_service.entity.Amenity;
import com.singeasy.booking_service.entity.KaraokeShop;
import com.singeasy.booking_service.entity.Room;
import com.singeasy.booking_service.enums.RoomStatus;
import com.singeasy.booking_service.repository.AmenityRepository;
import com.singeasy.booking_service.repository.BookingRepository;
import com.singeasy.booking_service.repository.RoomRepository;
import com.singeasy.booking_service.repository.ShopRepository;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private AmenityRepository amenityRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private RoomService roomService;

    @Test
    void addRoomToShop_savesRoom() {
        KaraokeShop shop = new KaraokeShop();
        shop.setId(1L);
        shop.setName("Shop A");

        RoomReqDto request = new RoomReqDto();
        request.setName("VIP 1");
        request.setAmenities(List.of("Wifi"));

        Room mappedRoom = new Room();
        mappedRoom.setName("VIP 1");
        mappedRoom.setAmenities(new ArrayList<>());

        Amenity wifi = new Amenity();
        wifi.setName("Wifi");

        Room savedRoom = buildRoom(10L, "VIP 1", RoomStatus.AVAILABLE.name());
        savedRoom.setAmenities(List.of(wifi));

        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));
        when(modelMapper.map(request, Room.class)).thenReturn(mappedRoom);
        when(amenityRepository.findByName("Wifi")).thenReturn(Optional.of(wifi));
        when(roomRepository.save(mappedRoom)).thenReturn(savedRoom);
        when(bookingRepository.findActiveBookingsByRoomAndDate(eq(10L), any())).thenReturn(List.of());

        RoomResDto result = roomService.addRoomToShop(1L, request);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("VIP 1");
        assertThat(result.getAmenities()).containsExactly("Wifi");
        verify(roomRepository).save(mappedRoom);
    }

    @Test
    void getRoomById_returnsRoom_whenActive() {
        Room room = buildRoom(1L, "Room 1", RoomStatus.AVAILABLE.name());
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(bookingRepository.findActiveBookingsByRoomAndDate(eq(1L), any())).thenReturn(List.of());

        RoomResDto result = roomService.getRoomById(1L);

        assertThat(result.getName()).isEqualTo("Room 1");
    }

    @Test
    void getRoomById_throws_whenDeleted() {
        Room room = buildRoom(1L, "Deleted Room", RoomStatus.DELETED.name());
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> roomService.getRoomById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Room has been deleted");
    }

    @Test
    void getRoomsByShop_filtersDeletedRooms() {
        Room active = buildRoom(1L, "Active", RoomStatus.AVAILABLE.name());
        Room deleted = buildRoom(2L, "Deleted", RoomStatus.DELETED.name());

        when(roomRepository.findByShopId(1L)).thenReturn(List.of(active, deleted));
        when(bookingRepository.findActiveBookingsByRoomAndDate(eq(1L), any())).thenReturn(List.of());

        List<RoomResDto> result = roomService.getRoomsByShop(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Active");
    }

    @Test
    void deleteRoom_setsStatusToDeleted() {
        Room room = buildRoom(5L, "Room 5", RoomStatus.AVAILABLE.name());
        when(roomRepository.findById(5L)).thenReturn(Optional.of(room));
        when(roomRepository.save(room)).thenReturn(room);

        roomService.deleteRoom(5L);

        assertThat(room.getStatus()).isEqualTo(RoomStatus.DELETED.name());
        verify(roomRepository).save(room);
    }

    @Test
    void getDistinctRoomCapacities_returnsDefaults_whenEmpty() {
        when(roomRepository.findDistinctCapacities()).thenReturn(List.of());

        assertThat(roomService.getDistinctRoomCapacities())
                .containsExactly("small", "medium", "large");
    }

    @Test
    void getDistinctRoomCapacities_returnsFromRepository_whenPresent() {
        when(roomRepository.findDistinctCapacities()).thenReturn(List.of("4", "8"));

        assertThat(roomService.getDistinctRoomCapacities()).containsExactly("4", "8");
    }

    private Room buildRoom(Long id, String name, String status) {
        Room room = new Room();
        room.setId(id);
        room.setName(name);
        room.setCapacity("10");
        room.setPricePerHour(200.0);
        room.setStatus(status);
        room.setImageUrl("http://image.url");
        room.setAmenities(new ArrayList<>());
        return room;
    }
}
