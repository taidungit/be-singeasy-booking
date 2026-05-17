package com.singeasy.booking_service.service;

import com.singeasy.booking_service.dto.req.UpdateProfileReqDto;
import com.singeasy.booking_service.dto.req.UserReqDto;
import com.singeasy.booking_service.dto.res.UserResDto;
import com.singeasy.booking_service.entity.User;
import com.singeasy.booking_service.repository.UserRepository;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }


    public List<UserResDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserResDto.class))
                .toList();
    }

    public UserResDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return modelMapper.map(user, UserResDto.class);
    }

    @Transactional
    public UserResDto createUser(UserReqDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        User user = modelMapper.map(dto, User.class);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        return modelMapper.map(userRepository.save(user), UserResDto.class);
    }

    @Transactional
    public UserResDto updateUser(Long id, UserReqDto dto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Cập nhật thông tin từ DTO vào Entity hiện tại
        modelMapper.map(dto, existingUser);
        dto.setRole(dto.getRole());
        // Nếu có thay đổi password thì phải mã hóa lại
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return modelMapper.map(userRepository.save(existingUser), UserResDto.class);
    }


    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    public User getUserByEmail(String email){
       return this.userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("User not found"));
    }

        public void updateUserToken(String token,String email){
        User currentUser=this.getUserByEmail(email);
        if(currentUser!=null){
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        }
    }
    public User getUserByRefreshTokenAndEmail(String token,String email){
        return this.userRepository.findByRefreshTokenAndEmail(token, email);
    }

        public boolean isEmailExist(String email){
        return this.userRepository.existsByEmail(email);
    }

    public UserResDto convertToResDto(User user) {
            if (user == null) return null;
            UserResDto dto = new UserResDto();
            dto.setId(user.getId());
            dto.setName(user.getName());
            dto.setEmail(user.getEmail());
            dto.setPhoneNumber(user.getPhoneNumber());
            dto.setAvatar(user.getAvatar());
            dto.setRole(user.getRole());
            return dto;
        }

    public User createUser(User user){
        return this.userRepository.save(user);
    }

    @Transactional
    public UserResDto updateProfile(String email, UpdateProfileReqDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Cập nhật thông tin chữ
        user.setName(dto.getName());
        user.setPhoneNumber(dto.getPhoneNumber());
        
        // Lưu chuỗi Base64 trực tiếp vào database (Cột avatar nên để LONGTEXT trong MySQL)
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }

        User savedUser = userRepository.save(user);
        return convertToResDto(savedUser);
    }
}