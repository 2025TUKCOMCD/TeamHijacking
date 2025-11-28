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

# --- 여기부터 시간대 설정 추가 ---
# 1. 시스템 시간대 설정 (권장하는 방법)
# 이 방법은 Java 애플리케이션뿐만 아니라 컨테이너 전체의 시스템 시간대를 설정합니다.
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 2. JVM 시작 옵션에 시간대 지정 (이전 답변에서 권장했던 방법)
# ENTRYPOINT 명령에 -Duser.timezone=Asia/Seoul 옵션을 추가합니다.
# 이미 위에서 시스템 시간대를 설정했으므로 중복일 수 있지만, 명시적인 설정을 위해 함께 사용해도 무방합니다.
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "/back/app.jar"]