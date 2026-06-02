-- 1. Tắt kiểm tra khóa ngoại tạm thời ở đầu file
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------
-- DỮ LIỆU BẢNG: amenity
-- -----------------------------------------------------
INSERT INTO `amenity` VALUES 
(1, NULL, 'Free Wi-Fi'),
(2, NULL, 'Air Conditioning'),
(3, NULL, 'Parking Lot'),
(4, NULL, 'Elevator'),
(5, NULL, 'JBL Sound System'),
(6, NULL, 'VIP Room'),
(7, NULL, 'Projector'),
(8, NULL, 'Food Service')
ON DUPLICATE KEY UPDATE name=VALUES(name), icon_url=VALUES(icon_url);

-- -----------------------------------------------------
-- DỮ LIỆU BẢNG: label
-- -----------------------------------------------------
INSERT INTO `label` VALUES 
(1, 'Budget'),
(2, 'Luxury'),
(3, 'Student'),
(4, 'Family'),
(5, 'Dating'),
(6, 'Trending')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 3. Bật lại kiểm tra khóa ngoại ở cuối file
SET FOREIGN_KEY_CHECKS = 1;