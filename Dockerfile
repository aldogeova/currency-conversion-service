# Use base image with JDK 21
FROM openjdk:21-jdk-slim

#Copy the compiled file
COPY build/libs/*.jar app.jar

# Expose port 9092
EXPOSE 9092

#Execute the app with prod profile
ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=prod", "app.jar"]
