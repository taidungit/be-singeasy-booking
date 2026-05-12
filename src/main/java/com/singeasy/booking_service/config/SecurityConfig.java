package com.singeasy.booking_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Tắt CSRF để có thể gọi API POST/PUT/DELETE từ React
            .csrf(csrf -> csrf.disable())
            
            // 2. Kích hoạt cấu hình CORS (nó sẽ lấy cấu hình từ WebMvcConfig của bạn)
            .cors(Customizer.withDefaults())
            
            // 3. Cho phép truy cập tự do vào các API (để dev cho nhanh)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/**").permitAll() 
                .anyRequest().permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Sử dụng BCrypt - thuật toán mã hóa mật khẩu tiêu chuẩn hiện nay
        return new BCryptPasswordEncoder();
    }
}