package org.fhir.uml.generation.uml;

import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.StringUtils;
import org.fhir.uml.generation.uml.elements.*;
import org.fhir.uml.generation.uml.types.RelationShipType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ElementDefinition;

import java.io.*;
import java.util.*;

public class SnapshotWrapper {
    private final Map<String, List<Element>> snapshotTableMap = new LinkedHashMap<>();
    private final Map<String, Element> elementMapper = new LinkedHashMap<>();
    private final UML UML = new UML();
    private final String resourceName;
    private final Legend legend = new Legend();
    private final String baseDefinition;
    private boolean firstElementProcessed = false;
    private final ElementFactory factory = ElementFactory.init(legend);

    public SnapshotWrapper(StructureDefinition structureDefinition) throws Exception {
        this.resourceName = structureDefinition.getName();
        this.baseDefinition = structureDefinition.getBaseDefinition().substring(
                structureDefinition.getBaseDefinition().lastIndexOf('/') + 1
        );
        structureDefinition.getSnapshot().getElement().forEach(this::processElementToSnapshotTable);

        for (Map.Entry<String, List<Element>> entry : snapshotTableMap.entrySet()) {
            Element umlElement = elementMapper.get(entry.getKey());
            Element parentUmlElement = elementMapper.get(umlElement.getParentId());
            if (umlElement.isRemoved() || parentUmlElement.isRemoved()) {
                continue;
            }

            UMLClass umlClass = new UMLClass(umlElement.getType(), umlElement.getName(), umlElement, parentUmlElement);
            entry.getValue().forEach(umlClass::addElement);
            UML.addClass(umlClass);

            UMLClass parentUmlClass = UML.findClassByElement(parentUmlElement);

            if (parentUmlClass != null && !parentUmlClass.equals(umlClass)) {
                UML.addRelation(Relation.from(
                        parentUmlClass,
                        umlClass,
                        RelationShipType.AGGREGATION,
                        umlElement.getName(),
                        umlElement.getCardinality()
                ));
            }
        }

        UML.setLegend(legend);
    }

    /**
     * Generates a UML diagram image (PNG) from the internal UML representation.
     * @param outputFilePath The path where the UML PNG file will be written.
     * @throws IOException if there's an error writing the output file.
     */
    public void generateUMLDiagram(String outputFilePath) throws IOException {
        SourceStringReader reader = new SourceStringReader(UML.toString());
        try (OutputStream png = new FileOutputStream(outputFilePath)) {
            // This generates the UML diagram as a PNG.
            reader.generateImage(png);
        }
    }

    /**
     * Saves the UML text (PlantUML syntax) to a .txt file.
     * @param outputFilePath The path where the UML text file will be written.
     * @throws IOException if there's an error writing the output file.
     */
    public void saveUMLAsText(String outputFilePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            writer.write(UML.toString());
        }
    }


    private void processElementToSnapshotTable(ElementDefinition element) {
        String id = element.getId();
        Element umlElement = factory.fromElementDefinition(element);

        if (id.contains(":")) {
            elementMapper.get(umlElement.getSliceParentId()).setSliceHeader(true);
        }

        if (!firstElementProcessed) {
            firstElementProcessed = true;
            umlElement.setType(baseDefinition);
            umlElement.setIsMain(true);
            snapshotTableMap.computeIfAbsent(umlElement.getParentId(), k -> new ArrayList<>());
            elementMapper.put(id, umlElement);
            return;
        }

        elementMapper.put(id, umlElement);
        snapshotTableMap.computeIfAbsent(umlElement.getParentId(), k -> new ArrayList<>())
                .add(umlElement);

        if (umlElement.isChoiseOfTypeHeader()) {
            umlElement.getDefinition().getType().forEach(type -> {
                String choiseId = umlElement.getElementId() + "." + type.getCode();
                Element choiseUML = new Element.Builder().choiseOfTypeElement(true).name(umlElement.getName().replace("[x]", "") + StringUtils.capitalize(type.getCode())).type(type.getCode()).build();
                snapshotTableMap.computeIfAbsent(umlElement.getElementId(), k -> new ArrayList<>()).add(choiseUML);
                elementMapper.put(choiseId, choiseUML);
            });
        }
    }
}
