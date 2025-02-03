# Step 1: Use a base image with JDK 17 to build the application
FROM gradle:8.0.0-jdk17 AS builder

# Set the working directory
WORKDIR /app

# Copy the build.gradle and settings.gradle files
COPY build.gradle settings.gradle ./

# Copy the source code
COPY src ./src

# Copy the Gradle Wrapper files
COPY gradlew gradlew.bat ./
COPY gradle ./gradle

# Make the gradlew script executable
RUN chmod +x gradlew

# Build the application without running tests
RUN ./gradlew build --no-daemon -x test

# Debugging: List the contents of the build/libs directory to verify the build output
RUN ls -l /app/build/libs

# Step 2: Use a smaller base image with JDK 17 for the final image
FROM openjdk:17-jdk-slim

# Copy the built application from the builder stage
COPY --from=builder /app/build/libs/*.jar /app/app.jar

# wait-for-it.sh 스크립트 복사
COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

ENTRYPOINT ["./wait-for-it.sh", "database:3306", "--", "java", "-jar", "/app/app.jar"]