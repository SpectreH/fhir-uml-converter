FROM eclipse-temurin:21-jdk-alpine

RUN apk add --no-cache graphviz

WORKDIR /app

COPY server/build/libs/fhir-uml-converter.jar /app/server.jar
COPY converter/build/libs/fhir-uml-generation.jar /app/fhir-uml-generation.jar

ENTRYPOINT ["java", "-jar", "/app/server.jar"]
