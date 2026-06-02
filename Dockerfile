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

# Cấu hình khởi tạo MySQL ban đầu
RUN service mysql start && \
    mysql -u root --execute="ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '123456789'; CREATE DATABASE IF NOT EXISTS karaoke_booking; FLUSH PRIVILEGES;"

# Cấu hình ép MySQL Server chạy ở chế độ siêu tiết kiệm RAM (Low Memory Profile)
RUN echo "[mysqld]\n\
performance_schema = OFF\n\
key_buffer_size = 8M\n\
max_connections = 10\n\
innodb_buffer_pool_size = 16M\n\
innodb_log_buffer_size = 2M\n\
query_cache_size = 0\n\
table_open_cache = 64\n\
thread_cache_size = 2\n" >> /etc/mysql/mysql.conf.d/mysqld.cnf

EXPOSE 8080

# Khởi động song song: Cấp tối đa 200MB RAM cho Java (-Xmx200m) để nhường phần còn lại cho MySQL và OS
CMD service mysql start && java -Xmx200m -jar app.jar