package org.fhir.uml.generation.uml.utils;

import net.sourceforge.plantuml.SourceStringReader;
import org.fhir.uml.generation.uml.elements.UML;

import java.io.*;

public class Utils {
    public static void generateUMLDiagram(UML uml, String outputFilePath) throws IOException {
        SourceStringReader reader = new SourceStringReader(uml.toString());
        try (OutputStream png = new FileOutputStream(outputFilePath)) {
           reader.outputImage(png);
        }
    }

    public static void saveUMLAsText(UML uml, String outputFilePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            writer.write(uml.toString());
        }
    }

    public static String capitalize(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
}
