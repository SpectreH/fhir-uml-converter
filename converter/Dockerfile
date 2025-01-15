# Используем базовый образ с JDK 17 (alpine - облегчённый)
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY build/libs/fhir-uml-generation.jar /app/converter.jar

CMD ["tail", "-f", "/dev/null"]
