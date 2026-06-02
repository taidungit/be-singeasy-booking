# --- Giai đoạn 1: Build file JAR từ Maven với JDK 21 ---
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# --- Giai đoạn 2: Tạo môi trường chạy chung Ubuntu 24.04 (Chứa cả Java 21 và MySQL) ---
FROM ubuntu:24.04

# Cài đặt MySQL Server và OpenJDK 21 JRE
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y \
    openjdk-21-jre-headless \
    mysql-server \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Cấu hình khởi chạy MySQL ban đầu và tạo Database
# (Sử dụng mật khẩu 123456789 và DB karaoke_booking khớp 100% với file local của bạn)
RUN service mysql start && \
    mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '123456789';" && \
    mysql -e "CREATE DATABASE IF NOT EXISTS karaoke_booking;" && \
    mysql -e "FLUSH PRIVILEGES;"

EXPOSE 8080

# Script khởi động song song cả MySQL Server và ứng dụng Spring Boot
CMD service mysql start && java -jar app.jar