<<<<<<< HEAD
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

=======
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

>>>>>>> 88100feb6f97c8e45d01dabfaa623e8dc25c5e1a
ENTRYPOINT ["java","-jar","app.jar"]