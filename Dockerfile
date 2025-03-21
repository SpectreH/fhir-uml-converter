FROM eclipse-temurin:21-jdk-alpine

# Install graphviz and curl
RUN apk add --no-cache graphviz curl

WORKDIR /app

# Download the specific PlantUML JAR and rename it
RUN curl -L https://github.com/plantuml/plantuml/releases/download/v1.2025.2/plantuml-1.2025.2.jar -o plantuml.jar

# Copy your other application artifacts into the container
COPY server/build/libs/fhir-uml-converter.jar /app/server.jar
COPY converter/build/libs/fhir-uml-generation.jar /app/fhir-uml-generation.jar

ENTRYPOINT ["java", "-jar", "/app/server.jar"]
