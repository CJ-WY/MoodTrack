# Stage 1: Build the application
FROM maven:3.8.6-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the final image
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/MoodTrack-1.0-SNAPSHOT.jar MoodTrack.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "MoodTrack.jar"]
