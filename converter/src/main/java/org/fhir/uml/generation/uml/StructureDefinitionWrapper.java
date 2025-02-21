package org.fhir.uml.generation.uml;

import net.sourceforge.plantuml.StringUtils;
import org.fhir.uml.generation.uml.elements.*;
import org.fhir.uml.generation.uml.types.RelationShipType;
import org.hl7.fhir.r4.model.StructureDefinition.*;
import org.hl7.fhir.r4.model.ElementDefinition;

import java.util.*;

public class StructureDefinitionWrapper {
    private final UML uml;
    private List<ElementDefinition> snapshotElements;
    private List<ElementDefinition> differentialElements;

    private final Map<String, String> fixedValues = new HashMap<>();

    private final Map<String, List<Element>> snapshotTableMap = new LinkedHashMap<>();
    private final Map<String, List<Element>> differentialTableMap = new LinkedHashMap<>();

    private final Map<String, Element> snapshotElementMapper = new LinkedHashMap<>();
    private final Map<String, Element> differentialElementMapper = new LinkedHashMap<>();

    private final ElementFactory factory;

    public StructureDefinitionWrapper(UML uml) throws Exception {
        this.uml = uml;
        this.factory = new ElementFactory(fixedValues);
    }

    public void proccessSnapshot(StructureDefinitionSnapshotComponent snapshotComponent) {
        this.snapshotElements = snapshotComponent.getElement();
        processElements(snapshotElements, snapshotTableMap, snapshotElementMapper);
    }

    public void proccessDifferential(StructureDefinitionDifferentialComponent differentialComponent) {
        this.differentialElements = differentialComponent.getElement();
        processElements(differentialElements, differentialTableMap, differentialElementMapper);
    }

    public void processElements(List<ElementDefinition> structureElements, Map<String, List<Element>> tableMap, Map<String, Element> elementMapper) {
        boolean firstClassPassed = false;
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

        for (Map.Entry<String, List<Element>> entry : tableMap.entrySet()) {
            Element umlElement = elementMapper.get(entry.getKey());
            Element parentUmlElement = elementMapper.get(umlElement.getParentId());
            if (umlElement.isRemoved() || parentUmlElement.isRemoved()) {
                continue;
            }

            System.out.printf("Class Type: %s | Name: %s\n", umlElement.getType(), umlElement.getName());
            UMLClass umlClass = new UMLClass(umlElement.getType(), umlElement.getName(), umlElement, parentUmlElement);

            if (!firstClassPassed) {
                firstClassPassed = true;
                umlClass.setMainClass(true);
            }

            entry.getValue().forEach(umlClass::addElement);
            uml.addClass(umlClass);

            UMLClass parentUmlClass = uml.findClassByElement(parentUmlElement);

            if (parentUmlClass != null && !parentUmlClass.equals(umlClass)) {
                uml.addRelation(Relation.from(
                        parentUmlClass,
                        umlClass,
                        RelationShipType.AGGREGATION,
                        umlElement.getName(),
                        umlElement.getCardinality()
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
