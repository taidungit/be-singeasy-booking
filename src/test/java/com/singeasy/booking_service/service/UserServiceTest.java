package com.singeasy.booking_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.singeasy.booking_service.dto.req.UpdateProfileReqDto;
import com.singeasy.booking_service.dto.req.UserReqDto;
import com.singeasy.booking_service.dto.res.UserResDto;
import com.singeasy.booking_service.entity.User;
import com.singeasy.booking_service.enums.RoleEnum;
import com.singeasy.booking_service.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void getAllUsers_returnsMappedUsers() {
        User user = buildUser(1L, "user@test.com");
        UserResDto dto = new UserResDto();
        dto.setEmail("user@test.com");

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(modelMapper.map(user, UserResDto.class)).thenReturn(dto);

        assertThat(userService.getAllUsers()).hasSize(1);
    }

    @Test
    void getUserById_returnsUser() {
        User user = buildUser(1L, "user@test.com");
        UserResDto dto = new UserResDto();
        dto.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserResDto.class)).thenReturn(dto);

        assertThat(userService.getUserById(1L).getId()).isEqualTo(1L);
    }

    @Test
    void createUser_encodesPasswordAndSaves() {
        UserReqDto request = new UserReqDto();
        request.setName("New User");
        request.setEmail("new@test.com");
        request.setPassword("plain-password");

        User mappedUser = new User();
        mappedUser.setEmail("new@test.com");
        User savedUser = buildUser(2L, "new@test.com");
        UserResDto response = new UserResDto();
        response.setEmail("new@test.com");

        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(modelMapper.map(request, User.class)).thenReturn(mappedUser);
        when(passwordEncoder.encode("plain-password")).thenReturn("hashed-password");
        when(userRepository.save(mappedUser)).thenReturn(savedUser);
        when(modelMapper.map(savedUser, UserResDto.class)).thenReturn(response);

        UserResDto result = userService.createUser(request);

        assertThat(result.getEmail()).isEqualTo("new@test.com");
        assertThat(mappedUser.getPassword()).isEqualTo("hashed-password");
        verify(passwordEncoder).encode("plain-password");
    }

    @Test
    void createUser_throws_whenEmailExists() {
        UserReqDto request = new UserReqDto();
        request.setEmail("exists@test.com");

        when(userRepository.findByEmail("exists@test.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already exists");
    }

    @Test
    void updateUser_throws_whenEmailTakenByAnotherUser() {
        User existing = buildUser(1L, "old@test.com");
        UserReqDto request = new UserReqDto();
        request.setEmail("taken@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already exists");
    }

    @Test
    void deleteUser_deletes_whenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_throws_whenNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void getUserByEmail_returnsUser() {
        User user = buildUser(1L, "user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        assertThat(userService.getUserByEmail("user@test.com").getEmail()).isEqualTo("user@test.com");
    }

    @Test
    void updateUserToken_updatesRefreshToken() {
        User user = buildUser(1L, "user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.updateUserToken("new-token", "user@test.com");

        assertThat(user.getRefreshToken()).isEqualTo("new-token");
        verify(userRepository).save(user);
    }

    @Test
    void isEmailExist_delegatesToRepository() {
        when(userRepository.existsByEmail("user@test.com")).thenReturn(true);

        assertThat(userService.isEmailExist("user@test.com")).isTrue();
    }

    @Test
    void convertToResDto_mapsFields() {
        User user = buildUser(1L, "user@test.com");
        user.setName("Test User");
        user.setRole(RoleEnum.USER);

        UserResDto result = userService.convertToResDto(user);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("user@test.com");
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getRole()).isEqualTo(RoleEnum.USER);
    }

    @Test
    void createUserEntity_setsDefaultRole() {
        User user = new User();
        user.setEmail("new@test.com");
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.createUser(user);

        assertThat(result.getRole()).isEqualTo(RoleEnum.USER);
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_updatesUserFields() {
        User user = buildUser(1L, "user@test.com");
        UpdateProfileReqDto request = new UpdateProfileReqDto();
        request.setName("Updated Name");
        request.setPhoneNumber("0900000000");
        request.setAvatar("avatar-base64");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResDto result = userService.updateProfile("user@test.com", request);

        assertThat(user.getName()).isEqualTo("Updated Name");
        assertThat(user.getPhoneNumber()).isEqualTo("0900000000");
        assertThat(user.getAvatar()).isEqualTo("avatar-base64");
        assertThat(result.getEmail()).isEqualTo("user@test.com");
    }

    private User buildUser(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setName("User");
        user.setRole(RoleEnum.USER);
        return user;
    }
}
