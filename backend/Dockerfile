# Use official Kotlin image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /backend

# Copy only the backend module
COPY backend /backend

# Install dependencies & build the backend
RUN chmod +x gradlew
RUN ./gradlew :backend:build

# Expose port (Railway assigns it automatically)
EXPOSE 8080

# Run backend server
CMD ["java", "-jar", "build/libs/backend-all.jar"]
