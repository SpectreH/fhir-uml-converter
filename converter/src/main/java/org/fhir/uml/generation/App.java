package org.fhir.uml.generation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.fhir.uml.generation.uml.FHIRGenerator;
import org.fhir.uml.generation.uml.StructureDefinitionWrapper;
import org.fhir.uml.generation.uml.elements.Element;
import org.fhir.uml.generation.uml.elements.Legend;
import org.fhir.uml.generation.uml.elements.UML;
import org.fhir.uml.generation.uml.utils.Config;
import org.fhir.uml.generation.uml.utils.Utils;
import org.hl7.fhir.r4.model.StructureDefinition;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;


public class App {
    private static Config config;
    public static void main(String[] args) throws Exception {
        config = Config.fromArgs(args);

        if (config.isShowHelp() || config.getInputFilePath() == null || config.getOutputFilePath() == null) {
            printUsage();
            return;
        }

        Map<String, Runnable> modeHandlers = new HashMap<>();
        modeHandlers.put("uml", App::runUmlMode);
        modeHandlers.put("fhir", App::runFhirMode);

        Runnable handler = modeHandlers.getOrDefault(config.getMode().toLowerCase(), App::runUmlMode);
        handler.run();
    }

    private static void runUmlMode() {
        try {
            FhirContext ctx = FhirContext.forR4();
            IParser parser = ctx.newJsonParser();

            StringBuilder jsonContent = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(config.getInputFilePath()))) {
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
            StructureDefinitionWrapper snapshotWrapper = new StructureDefinitionWrapper(structureDefinition, uml);
            snapshotWrapper.processSnapshot();
            snapshotWrapper.processDifferential();

            if (config.isDifferential()) {
                snapshotWrapper.mapDifferentialElementsWithSnapshotElements();
                snapshotWrapper.generateDifferentialUMLClasses();
            } else {
                snapshotWrapper.generateSnapshotUMLClasses();
            }

            uml.getMainClass().setName(Element.getURLLastPath(structureDefinition.getBaseDefinition()));
            uml.getMainClass().updateTitle();

            snapshotWrapper.generateUMLRelations();

            Legend legend = new Legend();
            legend.put("url", structureDefinition.getUrl());
            legend.put("version", structureDefinition.getVersion());
            legend.put("name", structureDefinition.getName());
            legend.put("status", structureDefinition.getStatus().getDisplay());
            legend.put("kind", structureDefinition.getKind().getDisplay());
            legend.put("type", structureDefinition.getType());
            legend.put("abstract", String.format("%s", structureDefinition.getAbstract()));
            legend.put("baseDefinition", structureDefinition.getBaseDefinition());

            uml.setLegend(legend);

            Utils.generateUMLDiagram(uml, config.getOutputFilePath());
            System.out.println("Processing complete. UML PNG file written to: " + config.getOutputFilePath());

            if (config.isSaveTxt()) {
                String txtOutputFilePath = config.getTxtOutputFilePath();
                if (txtOutputFilePath == null) {
                    txtOutputFilePath = config.getTxtOutputFilePath().replaceAll("\\.png$", ".txt");
                }
                Utils.saveUMLAsText(uml, txtOutputFilePath);
                System.out.println("PlantUML text also written to: " + txtOutputFilePath);
            }
        } catch (Exception e) {
            System.err.println("Error in UML mode: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runFhirMode() {
        try {
            if (config.isSaveTxt()) {
                System.out.println("Warning: --txt is not used in 'fhir' mode. Ignoring.");
            }

            StringBuilder umlContent = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(config.getInputFilePath()))) {
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

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(config.getOutputFilePath()))) {
                writer.write(structureDefinitionJson);
            }
            System.out.println("Transformation complete. FHIR StructureDefinition written to: " + config.getOutputFilePath());

        } catch (Exception e) {
            System.err.println("Error in FHIR mode: " + e.getMessage());
            e.printStackTrace();
        }
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
}
