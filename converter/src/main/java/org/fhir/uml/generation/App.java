package org.fhir.uml.generation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.fhir.uml.generation.uml.SnapshotWrapper;
import org.hl7.fhir.r4.model.StructureDefinition;

import java.io.*;

public class App {
    public static void main(String[] args) throws Exception {
        String inputFilePath = null;
        String outputFilePath = null;
        boolean saveTxt = false;             // Flag to determine whether we need a TXT output
        String txtOutputFilePath = null;     // Optional filename for the TXT file

        // Basic argument parsing
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--help".equalsIgnoreCase(arg)) {
                printUsage();
                return;
            } else if ("--input".equalsIgnoreCase(arg) && i + 1 < args.length) {
                inputFilePath = args[++i];
            } else if ("--output".equalsIgnoreCase(arg) && i + 1 < args.length) {
                outputFilePath = args[++i];
            } else if ("--txt".equalsIgnoreCase(arg)) {
                saveTxt = true;
                // Check if next argument is present and not another flag.
                if ((i + 1) < args.length && !args[i + 1].startsWith("--")) {
                    txtOutputFilePath = args[++i];
                }
            }
        }

        // Validate required parameters
        if (inputFilePath == null || outputFilePath == null) {
            System.err.println("Missing required parameters.");
            printUsage();
            return;
        }

        // Load the FHIR StructureDefinition from JSON
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();

        StringBuilder jsonContent = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                jsonContent.append(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading input file: " + e.getMessage());
            return;
        }

        StructureDefinition structureDefinition;
        try {
            structureDefinition = parser.parseResource(StructureDefinition.class, jsonContent.toString());
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            return;
        }

        // Process the StructureDefinition to generate UML (PNG output)
        SnapshotWrapper snapshotWrapper = new SnapshotWrapper(structureDefinition);
        snapshotWrapper.generateUMLDiagram(outputFilePath);
        System.out.println("Processing complete. UML PNG file written to: " + outputFilePath);

        // If the user asked to save the PlantUML text, save it as well
        if (saveTxt) {
            // If no txt filename was provided, create a default based on the .png filename
            if (txtOutputFilePath == null) {
                txtOutputFilePath = outputFilePath.replaceAll("\\.png$", ".txt");
            }
            snapshotWrapper.saveUMLAsText(txtOutputFilePath);
            System.out.println("PlantUML text also written to: " + txtOutputFilePath);
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java -jar fhir-uml-transformer.jar --input <path_to_fhir_json> --output <path_to_output_png> [--txt [<txt_filename>]]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --input     Path to the input FHIR StructureDefinition JSON file");
        System.out.println("  --output    Path to the output UML PNG file");
        System.out.println("  --txt       Also save the UML diagram in PlantUML text format. Optionally specify a custom .txt filename.");
        System.out.println("  --help      Show this help message");
    }
}
