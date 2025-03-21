package com.fhir.server.service;

import com.fhir.server.util.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Service
public class ConverterService {

    private static final Logger log = LoggerFactory.getLogger(ConverterService.class);

    @Value("${converter.name.jar}")
    private String converterJarName;

    @Value("${plantuml.name.jar}")
    private String PLANTUML_JAR = "plantuml.jar";

    private static final String INPUT_FILE_BASENAME = "input";
    private static final String OUTPUT_TEXT_BASENAME = "output";

    public byte[] convertFhirToUml(String body, Config config) throws IOException, InterruptedException {
        log.info("Starting convertFhirToUml. mode={}, view={}, exportAs={}, contentType={}",
                config.getMode(), config.getView(), config.getContentType(), config.getContentType());
        log.debug("FHIR input body (truncated): {}", body.length() > 200
                ? body.substring(0, 200) + "..." : body);

        Path inputFile = Files.createTempFile(INPUT_FILE_BASENAME, ".json");
        Path outputTxt = Files.createTempFile(OUTPUT_TEXT_BASENAME, ".txt");
        Path outputImage = Files.createTempFile(OUTPUT_TEXT_BASENAME, ".png");

        Files.writeString(inputFile, body, StandardCharsets.UTF_8);
        log.info("Wrote FHIR input to temp file: {}", inputFile);

        // 1) Run main converter
        ProcessResult converterResult = runConverterJar(inputFile, outputTxt, outputImage, config);
        log.info("Main converter finished with exitCode={}", converterResult.exitCode);

        if (converterResult.exitCode != 0) {
            log.error("Main converter jar failed. stderr:\n{}", converterResult.stderr);
            return buildFailedMessage(converterResult.exitCode, converterResult.stderr);
        }
        log.debug("Main converter stdout:\n{}", converterResult.stdout);
        System.out.println(converterResult.stdout);

        byte[] finalBytes;
        if (Objects.equals(config.getContentType(), MediaType.TEXT_PLAIN_VALUE)) {
            // If text/plain, read from outputTxt
            log.info("Reading text output from: {}", outputTxt);
            finalBytes = Files.readString(outputTxt, StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8);
        } else {
            // 2) Run PlantUML
            log.info("Calling PlantUML for image output...");
            ProcessResult plantUmlResult = runPlantUml(outputTxt, config);
            log.info("PlantUML finished with exitCode={}", plantUmlResult.exitCode);

            if (plantUmlResult.exitCode != 0) {
                log.error("PlantUML failed. stderr:\n{}", plantUmlResult.stderr);
                return buildFailedMessage(plantUmlResult.exitCode, plantUmlResult.stderr);
            }
            log.debug("PlantUML stdout:\n{}", plantUmlResult.stdout);

            boolean isPng = Objects.equals(config.getContentType(), MediaType.IMAGE_PNG_VALUE);
            String extension = isPng ? ".png" : ".svg";

            String inputName = outputTxt.getFileName().toString();   // e.g. "input1234.json"
            String baseNoExt = inputName.replaceAll("\\.\\w+$", ""); // "input1234"
            Path finalOutputPath = outputTxt.getParent().resolve(baseNoExt + extension);

            log.info("Reading final {} image from: {}", isPng ? "PNG" : "SVG", finalOutputPath);
            finalBytes = Files.readAllBytes(finalOutputPath);

            // Optionally remove the generated image
            Files.deleteIfExists(finalOutputPath);
            log.debug("Deleted plantUML output file: {}", finalOutputPath);
        }

        // Cleanup
        Files.deleteIfExists(inputFile);
        Files.deleteIfExists(outputTxt);
        log.debug("Cleaned up temp files: {}, {}", inputFile, outputTxt);

        return finalBytes;
    }

    public String convertUmlToFhir(String uml) {
        // not implemented
        return null;
    }

    private ProcessResult runConverterJar(Path inputFile, Path outputTxt, Path outputImg, Config config)
            throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder(
                "java", "-jar", converterJarName,
                "--mode", config.getMode(),
                "--input", inputFile.toAbsolutePath().toString(),
                "--output", outputImg.toAbsolutePath().toString(),
                "--txt", outputTxt.toAbsolutePath().toString(),
                "--view", config.getView(),
                "--hide_removed_objects", String.valueOf(config.getHideRemovedObjects()),
                "--show_constraints", String.valueOf(config.getShowConstraints()),
                "--show_bindings", String.valueOf(config.getShowBindings()),
                "--reduce_slice_classes", String.valueOf(config.getReduceSliceClasses()),
                "--hide_legend", String.valueOf(config.getHideLegend())
        );

        log.debug("Running main converter jar with command: {}", pb.command());
        System.out.println(pb.command());
        return runProcess(pb);
    }

    private ProcessResult runPlantUml(Path inputFile, Config config)
            throws IOException, InterruptedException {
        boolean isPng = Objects.equals(config.getContentType(), MediaType.IMAGE_PNG_VALUE);

        ProcessBuilder pb;
        if (isPng) {
                pb = new ProcessBuilder("java", "-jar", PLANTUML_JAR,
                    inputFile.toAbsolutePath().toString());
        } else {
            pb = new ProcessBuilder("java", "-jar", PLANTUML_JAR, inputFile.toAbsolutePath().toString(), "-tsvg");
        }
        log.debug("Running PlantUML with command: {}", pb.command());
        return runProcess(pb);
    }

    private ProcessResult runProcess(ProcessBuilder pb) throws IOException, InterruptedException {
        Process process = pb.start();

        try (InputStream is = process.getInputStream();
             InputStream es = process.getErrorStream()) {

            String stdout = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            String stderr = new String(es.readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.waitFor();

            return new ProcessResult(exitCode, stdout, stderr);
        }
    }

    private byte[] buildFailedMessage(int exitCode, String stderr) {
        String msg = "FAILED. exitCode=" + exitCode + "\n" + stderr;
        log.warn("Returning FAILED message: {}", msg);
        return msg.getBytes(StandardCharsets.UTF_8);
    }

    private static class ProcessResult {
        final int exitCode;
        final String stdout;
        final String stderr;
        ProcessResult(int exitCode, String stdout, String stderr) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
        }
    }
}
