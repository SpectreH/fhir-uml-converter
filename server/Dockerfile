FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY build/libs/fhir-uml-converter.jar /app/server.jar

ENTRYPOINT ["java", "-jar", "/app/server.jar"]
