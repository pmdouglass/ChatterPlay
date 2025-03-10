# Use an official Kotlin image
FROM gradle:8.5-jdk17 AS build

# Set work directory
WORKDIR /backend

# Copy the backend project files
COPY backend/ ./

# Give execution permission to Gradle
RUN chmod +x gradlew

# Build the backend project
RUN ./gradlew clean build -x check -x test

# Use an official JDK runtime for the final image
FROM openjdk:17-jdk-slim

# Set work directory
WORKDIR /backend

# Copy only the built jar file from previous step
COPY --from=build /backend/build/libs/backend-all.jar backend.jar

# Expose the port (Railway will dynamically assign one)
EXPOSE 8080

# Set the entry point to run the application
CMD ["java", "-jar", "backend.jar"]

# Expanded Code