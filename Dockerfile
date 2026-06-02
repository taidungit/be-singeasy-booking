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

# Khởi động cấu hình MySQL vượt qua cơ chế socket chứng thực của Ubuntu 24.04
RUN service mysql start && \
    mysql -u root --execute="ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '123456789'; CREATE DATABASE IF NOT EXISTS karaoke_booking; FLUSH PRIVILEGES;"

EXPOSE 8080

# Khởi động song song cả MySQL Server và ứng dụng Spring Boot khi container chạy
CMD service mysql start && java -jar app.jar