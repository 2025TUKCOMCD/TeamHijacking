# Step 1: Use a base image with JDK 17 to build the application
FROM gradle:8.0.0-jdk17 AS builder

# Set the working directory
WORKDIR /back

# Copy the build.gradle and settings.gradle files
COPY build.gradle settings.gradle ./

# Copy the Gradle Wrapper files
COPY gradlew gradlew.bat ./
COPY gradle ./gradle

# Copy the source code
COPY src ./src

# Make the gradlew script executable
RUN chmod +x gradlew

# Ensure the gradle-wrapper.properties file has the correct checksum
RUN sed -i 's/distributionSha256Sum=.*/distributionSha256Sum=2ebc6e46122c97b0e5e62fd6177878486942a72f9a8332e4c75d8fe555b11f13/' gradle/wrapper/gradle-wrapper.properties

# Download and install the Gradle distribution
RUN ./gradlew wrapper --gradle-version 8.12.1

# Build the application without running tests
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