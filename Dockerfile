# Use official Java 17 base image
FROM eclipse-temurin:17-jdk

# Set working directory inside container
WORKDIR /app

# Copy project files
COPY . .

# Build Spring Boot app (skip tests for speed)
RUN ./gradlew build -x test

# Run the app
CMD ["java", "-jar", "build/libs/IssueTalk-0.0.1-SNAPSHOT.jar"]
