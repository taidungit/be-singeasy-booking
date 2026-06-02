# --- Giai đoạn 1: Build file JAR từ Maven với JDK 21 ---
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# --- Giai đoạn 2: Tạo môi trường chạy chung Ubuntu 24.04 ---
FROM ubuntu:24.04

# Cài đặt MySQL Server và OpenJDK 21 JRE
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y \
    openjdk-21-jre-headless \
    mysql-server \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Khởi động và tạo Database ban đầu bằng quyền root mặc định
RUN service mysql start && \
    mysql -u root --execute="ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '123456789'; CREATE DATABASE IF NOT EXISTS karaoke_booking; FLUSH PRIVILEGES;"

EXPOSE 8080

# Chạy MySQL Server song song với Spring Boot, ép các tham số tiết kiệm RAM trực tiếp qua CLI
CMD mysqld --performance_schema=OFF --key_buffer_size=8M --innodb_buffer_pool_size=16M --innodb_log_buffer_size=2M --max_connections=10 & \
    sleep 5 && \
    java -Xmx256m -Xms128m -jar app.jar