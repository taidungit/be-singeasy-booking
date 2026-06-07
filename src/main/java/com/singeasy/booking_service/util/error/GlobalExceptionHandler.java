package com.singeasy.booking_service.util.error;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. BẮT CHÍNH XÁC LỖI ID INVALID (Trùng email, sai token,...) do bạn tự throw
    @ExceptionHandler(IdInvalidException.class)
    public ResponseEntity<Map<String, Object>> handleIdInvalidException(IdInvalidException ex) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("statusCode", HttpStatus.BAD_REQUEST.value()); // Trả về 400 chuẩn REST
        errors.put("error", "Bad Request");
        errors.put("message", ex.getMessage()); // 🔥 ĐƯA CHỮ "Email đã tồn tại" VÀO ĐÂY VỀ CHO FE

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // 2. BẮT CÁC LỖI VALIDATION ĐẦU VÀO (Nếu người dùng nhập thiếu trường, mật khẩu ngắn hơn quy định,...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Error");
        // Gom các lỗi validate lại thành một chuỗi văn bản
        response.put("message", fieldErrors.toString()); 

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 3. BẮT TẤT CẢ CÁC LỖI HỆ THỐNG PHÁT SINH KHÁC (Đề phòng crash ngầm)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errors.put("error", "Internal Server Error");
        errors.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errors);
    }
}