package org.fhir.uml.generation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.fhir.uml.generation.uml.FHIRGenerator;
import org.fhir.uml.generation.uml.StructureDefinitionWrapper;
import org.fhir.uml.generation.uml.elements.Element;
import org.fhir.uml.generation.uml.elements.Legend;
import org.fhir.uml.generation.uml.elements.UML;
import org.fhir.uml.generation.uml.types.LegendPosition;
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
            StructureDefinitionWrapper structureDefinitionWrapper = new StructureDefinitionWrapper(structureDefinition, uml);
            structureDefinitionWrapper.processSnapshot();
            structureDefinitionWrapper.processDifferential();

            if (config.isDifferential()) {
                structureDefinitionWrapper.mapDifferentialElementsWithSnapshotElements();
                if (config.isReduceSliceClasses()) {
                    structureDefinitionWrapper.reduceDifferentialSliceClasses();
                }
                structureDefinitionWrapper.generateDifferentialUMLClasses();
            } else {
                if (config.isReduceSliceClasses()) {
                    structureDefinitionWrapper.reduceSnapshotSliceClasses();
                }
                structureDefinitionWrapper.generateSnapshotUMLClasses();
            }

            uml.getMainClass().setName(Element.getURLLastPath(structureDefinition.getBaseDefinition()));

            structureDefinitionWrapper.generateUMLRelations();

            Legend legend = new Legend();
            legend.setXPosition(LegendPosition.XPosition.RIGHT);
            legend.setYPosition(LegendPosition.YPosition.TOP);

            legend.addGroup("StructureDefinition")
                    .setHeader("Type", "Value")
                    .addRow("url", "https://fhir.ee/base/StructureDefinition/ee-patient")
                    .addRow("version", "1.1.1")
                    .addRow("name", "EEBasePatient")
                    .addRow("status", "Draft")
                    .addRow("kind", "Resource")
                    .addRow("type", "Patient")
                    .addRow("abstract", "false")
                    .addRow("baseDefinition", "http://hl7.org/fhir/StructureDefinition/Patient");

            if (config.isShowConstraints()) {
                Legend.LegendGroup constraintGroup = legend.addGroup("Constraints");
                constraintGroup.setHeader("Key", "Severity", "Human");

                uml.getConstraints().values().forEach(constraint -> {
                    constraintGroup.addRow(constraint.getKey(), constraint.getSeverity(), String.format("wrap2(\"%s\", 50)", constraint.getHuman()));
                });
            }

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
