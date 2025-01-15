package com.example.fhir_uml_converter.service;

import com.example.fhir_uml_converter.util.ViewMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Service
public class ConverterService {
    @Value("${converter.name.jar}")
    private String converterJarName;

    public byte[] convertFhirToUml(String body, ViewMode viewMode, String requestedContentType) throws IOException, InterruptedException {
        Path inputFile = Files.createTempFile("input", ".json");
        Path outputPngFile = Files.createTempFile("output", ".png");
        Path outputTxtFile = Files.createTempFile("output", ".txt");
        Files.writeString(inputFile, body, StandardCharsets.UTF_8);

        ProcessBuilder pb = new ProcessBuilder(
                "java",
                "-jar",
                converterJarName,
                "--input",
                inputFile.toAbsolutePath().toString(),
                "--output",
                outputPngFile.toAbsolutePath().toString(),
                "--txt",
                outputTxtFile.toAbsolutePath().toString()
        );

        Process process = pb.start();

        // Считываем stdout (то, что конвертер пишет в стандартный вывод)
        String stdout;
        try (InputStream is = process.getInputStream()) {
            stdout = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        // Считываем stderr (то, что пишется в поток ошибок)
        String stderr;
        try (InputStream es = process.getErrorStream()) {
            stderr = new String(es.readAllBytes(), StandardCharsets.UTF_8);
        }

        // Ждём завершения процесса
        int exitCode = process.waitFor();

        // Если нужно проверить код выхода:
        if (exitCode != 0) {
            // Возможно, хотим вернуть пользователю stderr или stdout
            // в качестве сообщения об ошибке
            return ("FAILED. exitCode=" + exitCode + "\n" + stderr)
                    .getBytes(StandardCharsets.UTF_8);
        }

        // Если процесс завершился успешно, можно использовать stdout:
        StringBuilder builder = new StringBuilder();
        builder.append("Конвертер отработал, stdout:\n")
                .append(stdout);

        if (exitCode != 0) {
            // Можно считать поток ошибок и вернуть
            return "FAILED".getBytes(StandardCharsets.UTF_8);
        }

        byte[] resultBytes;
        if (Objects.equals(requestedContentType, MediaType.TEXT_PLAIN_VALUE)) {
            resultBytes = Files.readString(outputTxtFile, StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8);
        } else {
            resultBytes = Files.readAllBytes(outputPngFile);
        }

        Files.deleteIfExists(inputFile);
        Files.deleteIfExists(outputPngFile);

        return resultBytes;
    }

    public String convertUmlToFhir(String uml) {
        return null;
    }
}
