FROM openjdk:21-jdk
WORKDIR /app
COPY target/ms-loader-0.0.1-SNAPSHOT.jar ./ms-loader.jar
EXPOSE 8080
CMD ["java", "-jar", "ms-loader.jar"]