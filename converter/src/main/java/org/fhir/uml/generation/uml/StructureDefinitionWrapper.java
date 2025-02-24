package org.fhir.uml.generation.uml;

import net.sourceforge.plantuml.StringUtils;
import org.fhir.uml.generation.uml.elements.*;
import org.fhir.uml.generation.uml.types.RelationShipType;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.StructureDefinition.*;
import org.hl7.fhir.r4.model.ElementDefinition;

import java.util.*;

public class StructureDefinitionWrapper {
    private final UML uml;

    private StructureDefinition structureDefinition;

    private List<ElementDefinition> snapshotElements;
    private List<ElementDefinition> differentialElements;

    private final Map<String, String> fixedValues = new HashMap<>();

    private final Map<String, List<Element>> snapshotTableMap = new LinkedHashMap<>();
    private final Map<String, List<Element>> differentialTableMap = new LinkedHashMap<>();

    private final Map<String, Element> snapshotElementMapper = new LinkedHashMap<>();
    private final Map<String, Element> differentialElementMapper = new LinkedHashMap<>();

    private final ElementFactory factory;

    public StructureDefinitionWrapper(StructureDefinition structureDefinition, UML uml) throws Exception {
        this.structureDefinition = structureDefinition;
        this.uml = uml;
        this.factory = new ElementFactory(fixedValues);
    }

    public void processSnapshot() {
        this.snapshotElements = structureDefinition.getSnapshot().getElement();
        processElements(snapshotElements, snapshotTableMap, snapshotElementMapper);
    }

    public void processDifferential() {
        this.differentialElements = structureDefinition.getDifferential().getElement();
        processElements(differentialElements, differentialTableMap, differentialElementMapper);
    }

    private void processElements(List<ElementDefinition> structureElements, Map<String, List<Element>> tableMap, Map<String, Element> elementMapper) {
        boolean firstElementProcessed = false;
        expandAllFixedValues(structureElements);

        for (ElementDefinition elementDefinition : structureElements) {
            firstElementProcessed = processElementsToTables(
                    elementDefinition,
                    tableMap,
                    elementMapper,
                    firstElementProcessed
            );
        }
    }

    public void generateSnapshotUMLClasses() {
        generateUMLClasses(snapshotTableMap, snapshotElementMapper);
    }

    public void generateDifferentialUMLClasses() {
        generateUMLClasses(differentialTableMap, differentialElementMapper);
    }

    public void mapDifferentialElementsWithSnapshotElements() {
        List<String> keys = new ArrayList<>(differentialElementMapper.keySet());

        for (String key : keys) {
            Element differentialElement = differentialElementMapper.get(key);
            Element snapshotElement = snapshotElementMapper.get(key);

            if (differentialElement != null && snapshotElement != null) {
                differentialElement.copyValuesFrom(snapshotElement);
            }

            // Determine the parent from snapshot and set it in differential
            String parentId = (differentialElement != null) ? differentialElement.getParentId() : null;
            if (parentId != null) {
                Element parentElement = snapshotElementMapper.get(parentId);
                differentialElementMapper.computeIfAbsent(parentId, k -> parentElement);
            }

            String parentParentElementId = differentialElement.getElementId();
            String parentElementId = differentialElement.getElementId();
            while (true) {
                // Find the last '.' or the last ':'
                int lastDot = parentElementId.lastIndexOf('.');
                int lastColon = parentElementId.lastIndexOf(':');

                // Whichever is further to the right is the "last" delimiter
                int lastDelimiter = Math.max(lastDot, lastColon);

                // If no '.' or ':' is found, we're done
                if (lastDelimiter == -1) {
                    break;
                }

                parentElementId = parentElementId.substring(0, lastDelimiter);
                String finalParentParentElementId = parentParentElementId;
                differentialTableMap.computeIfAbsent(parentElementId, k -> new ArrayList<>(List.of(snapshotElementMapper.get(finalParentParentElementId))));
                differentialElementMapper.computeIfAbsent(parentElementId, snapshotElementMapper::get);
                differentialElementMapper.computeIfAbsent(parentParentElementId, snapshotElementMapper::get);

                parentParentElementId = parentElementId;
            }
        }
//
//        for (Map.Entry<String, List<Element>> entry : snapshotTableMap.entrySet()) {
//            System.out.println(entry);
//        }
//
//        System.out.println();
//        for (Map.Entry<String, List<Element>> entry : differentialTableMap.entrySet()) {
//            System.out.println(entry);
//        }
    }

    private void generateUMLClasses(Map<String, List<Element>> tableMap, Map<String, Element> elementMapper) {
        boolean firstClassPassed = false;
        for (Map.Entry<String, List<Element>> entry : tableMap.entrySet()) {
            Element umlElement = elementMapper.get(entry.getKey());
            Element parentUmlElement = elementMapper.get(umlElement.getParentId());

            System.out.printf("Class Type: %s | Name: %s | Parent Element: %s \n", umlElement.getType(), umlElement.getName(), parentUmlElement.getElementId());
            UMLClass umlClass = new UMLClass(umlElement.getType(), umlElement.getName(), umlElement, parentUmlElement, umlElement.isRemoved());

            if (!firstClassPassed) {
                firstClassPassed = true;
                umlClass.setMainClass(true);
            }

            entry.getValue().forEach(umlClass::addElement);
            uml.addClass(umlClass);
        }
    }

    public void generateUMLRelations() {
        for (UMLClass umlClass : uml.getClasses()) {
            UMLClass parentUmlClass = uml.findClassByElement(umlClass.getParentElement());
            if (parentUmlClass != null && !parentUmlClass.equals(umlClass)) {
                uml.addRelation(Relation.from(
                        parentUmlClass,
                        umlClass,
                        RelationShipType.AGGREGATION,
                        umlClass.getMainElement().getName(),
                        umlClass.getMainElement().getCardinality()
                ));
            }
        }
    }

    private void expandAllFixedValues(List<ElementDefinition> structureElements) {
        List<ElementDefinition> copyList = new ArrayList<>(structureElements);
        factory.defineFixedValues(copyList, structureElements);
    }

    private boolean processElementsToTables(ElementDefinition element, Map<String, List<Element>> tableMap, Map<String, Element> elementMapper, boolean firstElementProcessed) {
        String id = element.getId();
        Element umlElement = factory.fromElementDefinition(element);

        if (!element.getSlicing().getDiscriminator().isEmpty()) {
            umlElement.setSliceHeader(true);
        }

        if (!firstElementProcessed) {
            umlElement.setType(umlElement.getName());
            umlElement.setIsMain(true);
            tableMap.computeIfAbsent(umlElement.getParentId(), k -> new ArrayList<>()).add(umlElement);
            elementMapper.put(id, umlElement);
            return true;
        }

        elementMapper.put(id, umlElement);
        tableMap.computeIfAbsent(umlElement.getParentId(), k -> new ArrayList<>())
                .add(umlElement);

        if (umlElement.isChoiceOfTypeHeader()) {
            Arrays.stream(umlElement.getType().split(", ")).forEach(type -> {
                String choiseId = umlElement.getElementId() + "." + type;
                Element choiseUML = new Element.Builder().choiceOfTypeElement(true).name(umlElement.getName().replace("[x]", "") + StringUtils.capitalize(type)).type(type).build();
                tableMap.computeIfAbsent(umlElement.getElementId(), k -> new ArrayList<>()).add(choiseUML);
                elementMapper.put(choiseId, choiseUML);
            });
        }

        return true;
    }
}
