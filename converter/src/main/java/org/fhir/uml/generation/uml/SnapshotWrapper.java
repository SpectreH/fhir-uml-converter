package org.fhir.uml.generation.uml;

import net.sourceforge.plantuml.StringUtils;
import org.fhir.uml.generation.uml.elements.*;
import org.fhir.uml.generation.uml.types.RelationShipType;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ElementDefinition;

import java.util.*;

public class SnapshotWrapper {
    private final UML uml;
    private final StructureDefinition.StructureDefinitionSnapshotComponent snapshotComponent;
    private final Map<String, List<Element>> snapshotTableMap = new LinkedHashMap<>();
    private final Map<String, Element> elementMapper = new LinkedHashMap<>();
    private boolean firstElementProcessed = false;
    private final ElementFactory factory = ElementFactory.init();

    public SnapshotWrapper(StructureDefinition.StructureDefinitionSnapshotComponent snapshotComponent, UML uml) throws Exception {
        this.uml = uml;
        this.snapshotComponent = snapshotComponent;
    }

    public void processSnapshotElements() {
        boolean firstClassPassed = false;
//        this.resourceName = structureDefinition.getName();
//        this.baseDefinition = structureDefinition.getBaseDefinition().substring(
//                structureDefinition.getBaseDefinition().lastIndexOf('/') + 1
//        );
        snapshotComponent.getElement().forEach(this::processElementToSnapshotTable);

        for (Map.Entry<String, List<Element>> entry : snapshotTableMap.entrySet()) {
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

    private void processElementToSnapshotTable(ElementDefinition element) {
        String id = element.getId();
        Element umlElement = factory.fromElementDefinition(element);

        if (!element.getSlicing().getDiscriminator().isEmpty()) {
            umlElement.setSliceHeader(true);
        }

        if (!firstElementProcessed) {
            firstElementProcessed = true;
            umlElement.setType(umlElement.getName());
            umlElement.setIsMain(true);
            snapshotTableMap.computeIfAbsent(umlElement.getParentId(), k -> new ArrayList<>()).add(umlElement);
            elementMapper.put(id, umlElement);
            return;
        }

        elementMapper.put(id, umlElement);
        snapshotTableMap.computeIfAbsent(umlElement.getParentId(), k -> new ArrayList<>())
                .add(umlElement);

        if (umlElement.isChoiseOfTypeHeader()) {
            Arrays.stream(umlElement.getType().split(", ")).forEach(type -> {
                String choiseId = umlElement.getElementId() + "." + type;
                Element choiseUML = new Element.Builder().choiseOfTypeElement(true).name(umlElement.getName().replace("[x]", "") + StringUtils.capitalize(type)).type(type).build();
                snapshotTableMap.computeIfAbsent(umlElement.getElementId(), k -> new ArrayList<>()).add(choiseUML);
                elementMapper.put(choiseId, choiseUML);
            });
        }
    }
}
