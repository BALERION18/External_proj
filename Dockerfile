FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/student-registration-1.0.0.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
