# Step 1: Use a base image with JDK 17 to build the application
FROM gradle:8.0.0-jdk17 AS builder

# Set the working directory
WORKDIR /back

# --- Gradle Wrapper 및 설정 파일만 먼저 복사하여 캐시 활용 극대화 ---
# Copy Gradle Wrapper files
COPY gradlew gradlew.bat ./
COPY gradle ./gradle

# Copy only build configuration files
COPY build.gradle settings.gradle ./
# COPY gradle.properties ./ # 만약 있다면 추가

# Make the gradlew script executable
RUN chmod +x gradlew

# Ensure the gradle-wrapper.properties file has the correct checksum
RUN sed -i 's/distributionSha256Sum=.*/distributionSha256Sum=2ebc6e46122c97b0e5e62fd6177878486942a72f9a8332e4c75d8fe555b11f13/' gradle/wrapper/gradle-wrapper.properties

# Download and install the specified Gradle distribution (only if wrapper version changes)
# 이 단계는 gradle-wrapper.properties 파일이 변경될 때만 재실행됩니다.
RUN ./gradlew wrapper --gradle-version 8.12.1

# Download project dependencies to cache them. This runs a minimal build step.
# build.gradle이나 settings.gradle이 변경되지 않는 한 이 레이어는 캐시됩니다.
RUN ./gradlew dependencies --no-daemon

# --- 소스 코드 복사 (이전에 의존성이 캐시된 후) ---
# Copy the source code
COPY src ./src

# Build the application without running tests
# 소스 코드가 변경되면 이 단계부터 다시 실행됩니다.
RUN ./gradlew build --no-daemon -x test

# Step 2: Use a smaller base image with JDK 17 for the final image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /back

# Copy the built application from the builder stage
COPY --from=builder /back/build/libs/*.jar /back/app.jar

# Expose the port the application runs on
EXPOSE 8080

# Set the entry point to run the application
ENTRYPOINT ["java", "-jar", "/back/app.jar"]