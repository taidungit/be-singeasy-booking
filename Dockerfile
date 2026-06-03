# --- Giai đoạn 1: Build file JAR từ Maven với JDK 21 ---
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# --- Giai đoạn 2: Tạo môi trường chạy JRE 21 siêu nhẹ ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy file JAR từ giai đoạn build sang
COPY --from=build /app/target/*.jar app.jar

# Cổng chạy của Spring Boot
EXPOSE 8080

# Chạy trực tiếp file JAR, tối ưu RAM cho gói Free của Render (Giới hạn trong 300MB)
ENTRYPOINT ["java", "-Xmx300m", "-Xms150m", "-jar", "app.jar"]