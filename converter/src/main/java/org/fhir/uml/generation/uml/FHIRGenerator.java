package org.fhir.uml.generation.uml;

import org.fhir.uml.generation.uml.elements.Cardinality;
import org.fhir.uml.generation.uml.elements.Element;
import org.fhir.uml.generation.uml.elements.Element.Builder;
import org.fhir.uml.generation.uml.elements.Relation;
import org.fhir.uml.generation.uml.elements.UMLClass;
import org.fhir.uml.generation.uml.types.CustomClassType;
import org.fhir.uml.generation.uml.types.ElementVisability;
import org.fhir.uml.generation.uml.types.RelationShipType;
import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FHIRGenerator {
    private static final Pattern CLASS_PATTERN = Pattern.compile(
            "(?sm)(class|struct)\\s+\"([^\"]+)\"\\s*(<<.*?>>)?\\s*\\{([\\s\\S]*?)(?=^[}]\\s*$)",
            Pattern.MULTILINE
    );

    private static final Pattern FIELD_PATTERN = Pattern.compile(
            "(?m)^\\s*\\{([^}]*)}\\s+([+\\-~#])\\s+([^:\\s]+)\\s*:\\s*([^\\[=\\n]+)" +
                    "(?:\\s*=\\s*\\*\\*([^*]+)\\*\\*)?" +
                    "(?:\\s*\\[([^]]*)])?" +
                    "(?:\\s*<<([^>]+)>>)?\\s*$"
    );

    private static final Pattern BINDING_PATTERN = Pattern.compile(
            "(?m)^\\s*\\*\\*Binding\\*\\*:\\s*([^{]+)\\{([^}]+)}:\\s*//(.*?)//\\s*$"
    );

    private static final Pattern RELATION_PATTERN = Pattern.compile(
            "(?m)^\"([^\"]+)\"\\s*(--\\S+)\\s*\"([^\"]+)\"\\s*\"([^\"]+)\"\\s*:\\s*\\*\\*([^*]+)\\*\\*\\s*$"
    );

    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile(
            "^([^\"]+)\\s*\\(([^)]+)\\)$"
    );

    private static final Pattern CLASS_GROUP_PATTERN = Pattern.compile("(?m)^\\s*--(.*?)--\\s*$");

    public StructureDefinition parseUMLFile(String umlContent) {
        Matcher classMatcher = CLASS_PATTERN.matcher(umlContent);
        UML uml = new UML();
        boolean firstClass = true;
        while (classMatcher.find()) {
            boolean nextElementsAreSlices = false;
            String umlClassName = classMatcher.group(2);
            String customClass = classMatcher.group(3);
            String umlClassContent = classMatcher.group(4);

            String fhirClassName = "";
            String fhirClassType = "";

            Matcher classNameMatcher = CLASS_NAME_PATTERN.matcher(umlClassName);
            if (classNameMatcher.find()) {
                fhirClassName = classNameMatcher.group(2);
                fhirClassType = classNameMatcher.group(1);
            } else {
                fhirClassName = umlClassName;
            }

            UMLClass umlClass = new UMLClass(fhirClassType, fhirClassName, null, null);
            umlClass.setTitle(umlClassName);

            if (customClass != null) {
                umlClass.setCustomClassType(CustomClassType.fromUmlString(customClass));

            }

            String[] lines = umlClassContent.split("\\r?\\n");
            for (String line : lines) {
                Builder elementBuilder = new Builder();
                Matcher fieldMatcher = FIELD_PATTERN.matcher(line);
                Matcher bindingMatcher = BINDING_PATTERN.matcher(line);
                Matcher groupMatcher = CLASS_GROUP_PATTERN.matcher(line);

                if (fieldMatcher.matches()) {
                    String fieldVisibility = fieldMatcher.group(2);
                    String fieldName = fieldMatcher.group(3);
                    String fieldType = fieldMatcher.group(4);
                    String fieldFixedValue = fieldMatcher.group(5);
                    String fieldCardinality = fieldMatcher.group(6);

                    if (fieldType != null) {
                        fieldType = fieldType.strip();
                    }

                    if (fieldFixedValue != null) {
                        elementBuilder
                                .fixedValue(fieldFixedValue)
                                .hasFixedValue(true);
                    }

                    if (fieldCardinality != null) {
                        Cardinality cardinality = new Cardinality(fieldCardinality);
                        elementBuilder.cardinality(cardinality);
                    } else {
                        elementBuilder.choiseOfTypeElement(true);
                    }

                    ElementVisability elementVisability = ElementVisability.fromSymbol(fieldVisibility);

                    elementBuilder
                            .type(fieldType)
                            .name(fieldName)
                            .visability(elementVisability)
                            .hasSliceName(nextElementsAreSlices);

                    Element element = elementBuilder.build();
                    umlClass.addElement(element);
//                    System.out.format("Visibility: %s | Name: %s | Type: %s | Fixed Value: %s | Cardinality: %s \n", fieldVisibility, fieldName, fieldType, fieldFixedValue, fieldCardinality);

                    if (firstClass) {
                        umlClass.setMainClass(true);
                        element.setIsMain(true);
                        umlClass.setMainElement(element);
                        umlClass.setParentElement(element);
                        element.setId(element.getName());
                        element.setPath(element.getElementId());
                        firstClass = false;
                    }
                } else if (bindingMatcher.matches()) {
                    // TODO parse biding
                } else if (groupMatcher.matches()) {
                    if (groupMatcher.group(1).equals("Slices")) {
                        nextElementsAreSlices = true;
                    }
                }
            }
            uml.addClass(umlClass);
            // System.out.println(umlClass);
        }

        Matcher relationMatcher = RELATION_PATTERN.matcher(umlContent);
        while (relationMatcher.find()) {
            String relationFrom = relationMatcher.group(1);
            String relationTo = relationMatcher.group(4);
            String relationType = relationMatcher.group(2);
            String relationCardinality = relationMatcher.group(3);
            String relationFromElementName = relationMatcher.group(5);

            UMLClass fromClass = uml.findClassByTitle(relationFrom);
            UMLClass toClass = uml.findClassByTitle(relationTo);

            Relation.Builder relationBuilder = new Relation.Builder();
            relationBuilder
                    .from(fromClass)
                    .to(toClass)
                    .type(RelationShipType.fromArrow(relationType))
                    .relationLabel(relationFromElementName)
                    .cardinality(new Cardinality(relationCardinality));

            Relation relation = relationBuilder.build();
            uml.addRelation(relation);

            Element parentElement = fromClass.findElementByName(relationFromElementName);

            if (toClass.getTitle().contains("Slices for")) {
                parentElement.setSliceHeader(true);
            }

            toClass.setMainElement(parentElement);
            toClass.setMainElement(fromClass.getMainElement());
            toClass.setType(parentElement.getType());
            toClass.setName(parentElement.getName());
        }

        uml.getClasses().forEach(c -> c.updateTitle());

        UMLClass mainClass = uml.getMainClass();
        Element mainElement = mainClass.getMainElement();
        mainClass.getElements().forEach(e -> {
            if (!e.isMain()) {
                e.setId(String.format("%s.%s", mainElement.getElementId(), e.getName()));
                e.setPath(e.getElementId());
            }
        });

        uml.getRelations().forEach(r -> {
            UMLClass fromClass = r.getFrom();
            UMLClass toClass = r.getTo();
            Element parentElement = fromClass.findElementByName(r.getRelationLabel());
            toClass.getElements().forEach(e -> {
                e.setId(String.format("%s.%s", parentElement.getElementId(), e.getName()));
                e.setPath(e.getElementId());

                if (e.getHasSliceName()) {
                    e.setId(String.format("%s:%s", parentElement.getElementId(), e.getName()));
                    e.setPath(String.format("%s", parentElement.getPath()));
                }
            });
        });


        StructureDefinition sd = new StructureDefinition();
        StructureDefinition.StructureDefinitionSnapshotComponent snapshot = new StructureDefinition.StructureDefinitionSnapshotComponent();
        List<ElementDefinition> snapshotElements = new ArrayList<>();

        sd.setName("Test");

        uml.getClasses().forEach(c -> {
            c.getElements().forEach(e -> {
                ElementDefinition elementDefinition = new ElementDefinition();
                elementDefinition.setId(e.getElementId());
                elementDefinition.setPath(e.getPath());
                if (e.getCardinality() != null) {
                    elementDefinition.setMin(Integer.parseInt(e.getCardinality().getMin()));
                    elementDefinition.setMax(e.getCardinality().getMax());
                }

                ElementDefinition.TypeRefComponent elementType = new ElementDefinition.TypeRefComponent();
                elementType.setCode(e.getType());

                elementDefinition.setType(List.of(elementType));
                snapshotElements.add(elementDefinition);
            });
        });

        snapshot.setElement(snapshotElements);
        sd.setSnapshot(snapshot);

//        uml.getClasses().forEach(c ->  System.out.format("Class Type: %s | Name: %s \n", c.getType(), c.getName()));
//        System.out.println(umlContent);
        return sd;
    }
}
