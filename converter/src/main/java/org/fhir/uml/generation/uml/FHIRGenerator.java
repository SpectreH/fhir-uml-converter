package org.fhir.uml.generation.uml;

import org.fhir.uml.generation.uml.elements.*;
import org.fhir.uml.generation.uml.elements.Element.Builder;
import org.fhir.uml.generation.uml.types.CustomClassType;
import org.fhir.uml.generation.uml.types.ElementVisability;
import org.fhir.uml.generation.uml.types.RelationShipType;
import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.StructureDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Responsible for reading UML-like text and constructing a
 * FHIR StructureDefinition (R4 model) from it.
 */
public class FHIRGenerator {

    // -------------------------------------------------------------------------
    // Regex Patterns
    // -------------------------------------------------------------------------
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

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Parses the UML-like text and builds a FHIR StructureDefinition.
     *
     * @param umlContent the UML text (PlantUML-style) to parse
     * @return a generated StructureDefinition
     */
    public StructureDefinition parseUMLFile(String umlContent) {
        UML uml = new UML();

        // Parse classes (and their fields)
        parseClasses(umlContent, uml);

        // Parse relations
        parseRelations(umlContent, uml);

        // Post-process UML to finalize IDs/paths, etc.
        postProcessUML(uml);

        // Convert the UML model into a StructureDefinition
        return buildStructureDefinition(uml);
    }

    // -------------------------------------------------------------------------
    // Step 1: Parse Classes
    // -------------------------------------------------------------------------
    private void parseClasses(String umlContent, UML uml) {
        Matcher classMatcher = CLASS_PATTERN.matcher(umlContent);
        boolean firstClass = true;

        while (classMatcher.find()) {
            String umlClassName = classMatcher.group(2);
            String customClassType = classMatcher.group(3);
            String umlClassBody = classMatcher.group(4);

            String parsedFhirClassName;
            String parsedFhirClassType = "";
            Matcher classNameMatcher = CLASS_NAME_PATTERN.matcher(umlClassName);
            if (classNameMatcher.find()) {
                parsedFhirClassType = classNameMatcher.group(1);
                parsedFhirClassName = classNameMatcher.group(2);
            } else {
                parsedFhirClassName = umlClassName;
            }

            UMLClass umlClass = new UMLClass(parsedFhirClassType, parsedFhirClassName, null, null, false);;

            if (customClassType != null) {
                umlClass.setCustomClassType(CustomClassType.fromUmlString(customClassType));
            }

            if (firstClass) {
                umlClass.setMainClass(true);
                Element mainElement = new Builder()
                        .id(parsedFhirClassName)
                        .isMain(true)
                        .name(parsedFhirClassName)
                        .type(parsedFhirClassType.strip())
                        .cardinality(new Cardinality("0","*")).build();
                umlClass.addElement(mainElement);
                umlClass.setMainElement(mainElement);
                firstClass = false;
            }

            parseClassBody(umlClassBody, umlClass);

            uml.addClass(umlClass);
        }
    }

    /**
     * Helper to parse the lines within a UML class body
     * and populate the given UMLClass with elements, etc.
     */
    private void parseClassBody(String classBody, UMLClass umlClass) {
        String[] lines = classBody.split("\\r?\\n");
        boolean nextElementsAreSlices = false;

        for (String line : lines) {
            Matcher fieldMatcher = FIELD_PATTERN.matcher(line);
            Matcher bindingMatcher = BINDING_PATTERN.matcher(line);
            Matcher groupMatcher = CLASS_GROUP_PATTERN.matcher(line);

            // Check for field lines
            if (fieldMatcher.matches()) {
                String fieldVisibility = fieldMatcher.group(2);
                String fieldName = fieldMatcher.group(3);
                String fieldType = fieldMatcher.group(4);
                String fieldFixedValue = fieldMatcher.group(5);
                String fieldCardinality = fieldMatcher.group(6);

                // Build up the element
                Element.Builder elementBuilder = new Builder();

                if (fieldFixedValue != null && !fieldFixedValue.isBlank()) {
                    elementBuilder.fixedValue(fieldFixedValue).hasFixedValue(true);
                }

                if (fieldCardinality != null && !fieldCardinality.isBlank()) {
                    elementBuilder.cardinality(new Cardinality(fieldCardinality));
                } else {
                    // If no cardinality, it might be a choice element
                    elementBuilder.choiceOfTypeElement(true);
                }

                elementBuilder
                        .type(fieldType != null ? fieldType.strip() : null)
                        .name(fieldName)
                        .visibility(ElementVisability.fromSymbol(fieldVisibility))
                        .hasSliceName(nextElementsAreSlices);

                Element element = elementBuilder.build();
                umlClass.addElement(element);
            }
            // Check for binding lines (TODO: parse as needed)
            else if (bindingMatcher.matches()) {
                // e.g. "**Binding**: SomeBinding{CodeSystem}://SomeDefinition//"
                // Expand as needed.
            }
            // Check for groups (e.g. "--Slices--")
            else if (groupMatcher.matches()) {
                if ("Slices".equals(groupMatcher.group(1))) {
                    nextElementsAreSlices = true;
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Step 2: Parse Relations
    // -------------------------------------------------------------------------
    private void parseRelations(String umlContent, UML uml) {
        Matcher relationMatcher = RELATION_PATTERN.matcher(umlContent);
        while (relationMatcher.find()) {
            String relationFrom = relationMatcher.group(1);
            String arrowType = relationMatcher.group(2);
            String relationCardinality = relationMatcher.group(3);
            String relationTo = relationMatcher.group(4);
            String relationFromElementName = relationMatcher.group(5);

            UMLClass fromClass = uml.findClassByTitle(relationFrom);
            UMLClass toClass = uml.findClassByTitle(relationTo);

            // Build the Relation
            Relation.Builder relationBuilder = new Relation.Builder()
                    .from(fromClass)
                    .to(toClass)
                    .type(RelationShipType.fromArrow(arrowType))
                    .relationLabel(relationFromElementName)
                    .cardinality(new Cardinality(relationCardinality));

            Relation relation = relationBuilder.build();
            uml.addRelation(relation);

            // Handle slice if "Slices for ...", etc.
            Element parentElement = fromClass.findElementByName(relationFromElementName);
            if (parentElement != null && toClass.getTitle().contains("Slices for")) {
                parentElement.setSliceHeader(true);
            }

            // Set main element references
            toClass.setMainElement(parentElement);
            toClass.setMainElement(fromClass.getMainElement());
            toClass.setType(parentElement != null ? parentElement.getType() : null);
            toClass.setName(parentElement != null ? parentElement.getName() : toClass.getName());
        }
    }

    // -------------------------------------------------------------------------
    // Step 3: Post-process the UML model
    // -------------------------------------------------------------------------
    private void postProcessUML(UML uml) {
        // The "main class" is typically the first one that was flagged
        UMLClass mainClass = uml.getMainClass();
        if (mainClass == null) {
            return;
        }

        Element mainElement = mainClass.getMainElement();

        // Update IDs/paths for elements in the main class
        mainClass.getElements().forEach(e -> {
            if (!e.isMain()) {
                e.setId(String.format("%s.%s", mainElement.getElementId(), e.getName()));
                e.setPath(e.getElementId());
            }
        });

        // For each relation, update the child class's elements
        uml.getRelations().forEach(r -> {
            UMLClass fromClass = r.getFrom();
            UMLClass toClass = r.getTo();
            Element parentElement = fromClass.findElementByName(r.getRelationLabel());
            if (parentElement == null) {
                return;
            }

            toClass.getElements().forEach(e -> {
                e.setId(String.format("%s.%s", parentElement.getElementId(), e.getName()));
                e.setPath(e.getElementId());

                if (e.getHasSliceName()) {
                    // Example: "parentId:fieldName"
                    e.setId(String.format("%s:%s", parentElement.getElementId(), e.getName()));
                    e.setPath(parentElement.getPath());
                }
            });
        });
    }

    // -------------------------------------------------------------------------
    // Step 4: Build the final StructureDefinition
    // -------------------------------------------------------------------------
    private StructureDefinition buildStructureDefinition(UML uml) {
        // Construct a new StructureDefinition
        StructureDefinition sd = new StructureDefinition();
        sd.setName("Test"); // Example name; set as needed

        // Prepare the snapshot component
        StructureDefinition.StructureDefinitionSnapshotComponent snapshot =
                new StructureDefinition.StructureDefinitionSnapshotComponent();
        List<ElementDefinition> snapshotElements = new ArrayList<>();

        // Convert each UML element into an ElementDefinition
        for (UMLClass c : uml.getClasses()) {
            for (Element e : c.getElements()) {
                ElementDefinition elementDefinition = new ElementDefinition();

                elementDefinition.setId(e.getElementId());
                elementDefinition.setPath(e.getPath());

                // If cardinalities are present, set them
                if (e.getCardinality() != null) {
                    try {
                        elementDefinition.setMin(
                                Integer.parseInt(e.getCardinality().getMin())
                        );
                        elementDefinition.setMax(e.getCardinality().getMax());
                    } catch (NumberFormatException ex) {
                        // handle or log error if min is not a number
                    }
                }

                // Set type
                if (e.getType() != null) {
                    ElementDefinition.TypeRefComponent elementType =
                            new ElementDefinition.TypeRefComponent();
                    elementType.setCode(e.getType());
                    elementDefinition.setType(List.of(elementType));
                }

                snapshotElements.add(elementDefinition);
            }
        }

        snapshot.setElement(snapshotElements);
        sd.setSnapshot(snapshot);

        return sd;
    }
}
