package com.singeasy.booking_service.controller; // Sửa lại package cho đúng dự án của bạn

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dev-tools")
@CrossOrigin(origins = "*") // Cho phép gọi từ mọi nơi để tiện test
public class DatabaseViewerController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 1. API xem danh sách tất cả các bảng đang có trong DB
    // URL: https://be-singeasy-booking.onrender.com/api/v1/dev-tools/tables
    @GetMapping("/tables")
    public List<Map<String, Object>> showTables() {
        return jdbcTemplate.queryForList("SHOW TABLES");
    }

    // 2. API xem toàn bộ dữ liệu của một bảng bất kỳ thông qua tên bảng truyền vào URL
    // URL ví dụ: https://be-singeasy-booking.onrender.com/api/v1/dev-tools/data/capacities
    @GetMapping("/data/{tableName}")
    public List<Map<String, Object>> getTableData(@PathVariable String tableName) {
        // Kiểm tra bảo mật cơ bản để tránh SQL Injection vào tên bảng
        if (!tableName.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Tên bảng không hợp lệ!");
        }
        
        String sql = "SELECT * FROM " + tableName;
        return jdbcTemplate.queryForList(sql);
    }
}