package org.fhir.uml.generation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.fhir.uml.generation.uml.FHIRGenerator;
import org.fhir.uml.generation.uml.SnapshotWrapper;
import org.fhir.uml.generation.uml.elements.UML;
import org.fhir.uml.generation.uml.Utils;
import org.hl7.fhir.r4.model.StructureDefinition;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class App {
    public static void main(String[] args) throws Exception {
        AppArguments appArgs = parseArguments(args);

        if (appArgs.showHelp || appArgs.inputFilePath == null || appArgs.outputFilePath == null) {
            printUsage();
            return;
        }

        Map<String, Consumer<AppArguments>> modeHandlers = new HashMap<>();
        modeHandlers.put("uml", App::runUmlMode);
        modeHandlers.put("fhir", App::runFhirMode);

        Consumer<AppArguments> handler = modeHandlers.getOrDefault(
                appArgs.mode.toLowerCase(),
                App::runUmlMode
        );

        handler.accept(appArgs);
    }

    private static void runUmlMode(AppArguments appArgs) {
        try {
            FhirContext ctx = FhirContext.forR4();
            IParser parser = ctx.newJsonParser();

            StringBuilder jsonContent = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(appArgs.inputFilePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    jsonContent.append(line);
                }
            }

            StructureDefinition structureDefinition = parser.parseResource(
                    StructureDefinition.class,
                    jsonContent.toString()
            );

            UML uml = new UML();
            SnapshotWrapper snapshotWrapper = new SnapshotWrapper(structureDefinition.getSnapshot(), uml);
            snapshotWrapper.processSnapshotElements();
            Utils.generateUMLDiagram(uml, appArgs.outputFilePath);
            System.out.println("Processing complete. UML PNG file written to: " + appArgs.outputFilePath);

            if (appArgs.saveTxt) {
                String txtOutputFilePath = appArgs.txtOutputFilePath;
                if (txtOutputFilePath == null) {
                    txtOutputFilePath = appArgs.outputFilePath.replaceAll("\\.png$", ".txt");
                }
                Utils.saveUMLAsText(uml, txtOutputFilePath);
                System.out.println("PlantUML text also written to: " + txtOutputFilePath);
            }
        } catch (Exception e) {
            System.err.println("Error in UML mode: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runFhirMode(AppArguments appArgs) {
        try {
            if (appArgs.saveTxt) {
                System.out.println("Warning: --txt is not used in 'fhir' mode. Ignoring.");
            }

            StringBuilder umlContent = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(appArgs.inputFilePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    umlContent.append(line).append("\n");
                }
            }

            FHIRGenerator generator = new FHIRGenerator();
            StructureDefinition structureDefinition = generator.parseUMLFile(umlContent.toString());
            FhirContext ctx = FhirContext.forR4();
            IParser parser = ctx.newJsonParser().setPrettyPrint(true);
            String structureDefinitionJson = parser.encodeResourceToString(structureDefinition);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(appArgs.outputFilePath))) {
                writer.write(structureDefinitionJson);
            }
            System.out.println("Transformation complete. FHIR StructureDefinition written to: " + appArgs.outputFilePath);

        } catch (Exception e) {
            System.err.println("Error in FHIR mode: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static AppArguments parseArguments(String[] args) {
        AppArguments appArgs = new AppArguments();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg.toLowerCase()) {
                case "--help":
                    appArgs.showHelp = true;
                    break;
                case "--mode":
                    if (i + 1 < args.length) {
                        appArgs.mode = args[++i];
                    }
                    break;
                case "--input":
                    if (i + 1 < args.length) {
                        appArgs.inputFilePath = args[++i];
                    }
                    break;
                case "--output":
                    if (i + 1 < args.length) {
                        appArgs.outputFilePath = args[++i];
                    }
                    break;
                case "--txt":
                    appArgs.saveTxt = true;
                    if ((i + 1) < args.length && !args[i + 1].startsWith("--")) {
                        appArgs.txtOutputFilePath = args[++i];
                    }
                    break;
            }
        }
        return appArgs;
    }

    private static StructureDefinition createStructureDefinitionFromUml(String umlText) {
        // TODO: Implement UML -> StructureDefinition converter
        StructureDefinition sd = new StructureDefinition();
        return sd;
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java -jar fhir-uml-transformer.jar --mode <uml|fhir> --input <input_file> --output <output_file> [--txt [<txt_filename>]]");
        System.out.println();
        System.out.println("Modes:");
        System.out.println("  uml (default): Transform FHIR StructureDefinition -> UML");
        System.out.println("    --input   Path to the input FHIR StructureDefinition JSON file");
        System.out.println("    --output  Path to the output UML PNG file");
        System.out.println("    --txt     Also save the UML diagram in PlantUML text format. Optionally specify a custom .txt filename.");
        System.out.println();
        System.out.println("  fhir: Transform UML -> FHIR StructureDefinition");
        System.out.println("    --input   Path to the input UML (PlantUML) .txt file");
        System.out.println("    --output  Path to the output FHIR StructureDefinition JSON file (can use .json or .txt extension)");
        System.out.println("    --txt     Not used in 'fhir' mode (ignored if specified).");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --help   Show this help message");
    }

    private static class AppArguments {
        String mode = "uml";            // Default mode
        String inputFilePath;
        String outputFilePath;
        boolean saveTxt = false;
        String txtOutputFilePath;
        boolean showHelp = false;
    }
}
