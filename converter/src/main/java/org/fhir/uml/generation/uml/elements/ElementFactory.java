package org.fhir.uml.generation.uml.elements;

import org.fhir.uml.generation.uml.types.ElementVisability;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.r4.model.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A non-static factory that knows how to create Elements and optionally
 * expand fixed[x] definitions into sub-element definitions in the snapshot.
 */
public class ElementFactory {
    private final Map<String, String> fixedValues;

    /**
     * Конструктор, сохраняющий ссылку на Map для фиксированных значений
     * и на snapshot для добавления новых ElementDefinition.
     */
    public ElementFactory(Map<String, String> fixedValues) {
        this.fixedValues = fixedValues;
    }

    /**
     * Преобразует ElementDefinition в наш класс Element,
     * при необходимости вызывая parseFixedValues для заполнения Map и snapshot.
     */
    public Element fromElementDefinition(ElementDefinition elementDefinition) {
        String path = elementDefinition.getPath();
        String id = elementDefinition.getId();

        // Extract name from element ID.
        String[] idParts = id.split("\\.|:");
        String extractedName = (idParts.length == 0) ? null : idParts[idParts.length - 1];
//        System.out.println(id);


        // Cardinality
        Cardinality extractedCardinality = new Cardinality(
                String.valueOf(elementDefinition.getMinElement().getValue()),
                elementDefinition.getMax()
        );

        // Type resolution
        String extractedType = Element.resolveType(elementDefinition);

        // Check if it is a "choice of type" header
        // (multiple types, not including Reference/canonical).
        boolean choiceOfTypeHeader = hasMultipleNonReferenceTypes(elementDefinition);

        // Determine visibility
        ElementVisability visibility = determineVisibility(extractedType);

        // Fixed values handling
        boolean hasFixedValue = false;
        String fixedValue = "";
        if (fixedValues.containsKey(id)) {
            fixedValue = fixedValues.get(id);
            fixedValues.remove(id);
            hasFixedValue = true;
            visibility = ElementVisability.PROTECTED;
        }

        // Build and return the Element
        return new Element.Builder()
                .name(extractedName)
                .type(extractedType)
                .visibility(visibility)
                .cardinality(extractedCardinality)
                .hasFixedValue(hasFixedValue)
                .fixedValue(fixedValue)
                .choiceOfTypeHeader(choiceOfTypeHeader)
                .choiceOfTypeElement(false)
                .id(id)
                .hasSliceName(hasSliceName(elementDefinition))
                .build();
    }

    public void defineFixedValues(List<ElementDefinition> copyList, List<ElementDefinition> structureElements) {
        for (ElementDefinition ed : copyList) {
            if (ed.hasFixed()) {
                parseFixedValues(ed.getFixed(), ed.getId(), ed.getPath(), structureElements);
            }
        }
    }

    /**
     * Рекурсивно обходит fixedType, добавляет его в Map (если примитив),
     * а также создаёт и добавляет новые ElementDefinition в snapshotComponent
     * для каждого уровня вложенности.
     */
    private void parseFixedValues(Type fixedType, String id, String path, List<ElementDefinition> structureElements) {
        if (fixedType instanceof PrimitiveType) {
            handlePrimitiveFixedType((PrimitiveType<?>) fixedType, id, path, structureElements);
            return;
        }

        // Handle complex fixed type
        addElementDefinitionForFixedType(fixedType, id, path, structureElements);

        // Recursively parse children
        List<Property> children = fixedType.children();
        for (Property childProp : children) {
            String childName = childProp.getName();
            List<Base> values = childProp.getValues();

            for (int i = 0; i < values.size(); i++) {
                IBase childValue = values.get(i);

                String nextId = id + "." + childName;
                String nextPath = path + "." + childName;
                // If there are multiple values, add an index
                if (values.size() > 1) {
                    nextPath += "[" + i + "]";
                }

                if (childValue instanceof Type) {
                    parseFixedValues((Type) childValue,nextId, nextPath, structureElements);
                }
                // If childValue is a Resource or something else,
                // handle accordingly (not typical for fixed, but possible).
            }
        }
    }

    // ----------------------------------
    // Private helper methods
    // ----------------------------------

    /**
     * Checks if the given ElementDefinition has multiple types
     * that are not Reference or canonical.
     */
    private boolean hasMultipleNonReferenceTypes(ElementDefinition elementDefinition) {
        List<ElementDefinition.TypeRefComponent> types = elementDefinition.getType();
        if (types.size() <= 1) {
            return false;
        }
        // If any type is not a reference/canonical, we consider it a choice.
        // (Based on original logic that lumps them separately.)
        return types.stream().noneMatch(typeRef ->
                typeRef.getCode().contains("Reference") || typeRef.getCode().contains("canonical")
        );
    }

    /**
     * Determines visibility based on the extractedType.
     */
    private ElementVisability determineVisibility(String extractedType) {
        if (extractedType.contains("Reference") || extractedType.contains("canonical")) {
            return ElementVisability.PACKAGE_PRIVATE;
        }
        return ElementVisability.PUBLIC;
    }

    /**
     * Checks if the given ElementDefinition has a slice name.
     */
    private boolean hasSliceName(ElementDefinition elementDefinition) {
        return elementDefinition.getSliceName() != null
                && !elementDefinition.getSliceName().isEmpty();
    }

    /**
     * Handles the case when the fixed type is a primitive.
     */
    private void handlePrimitiveFixedType(PrimitiveType<?> primitive, String id, String path, List<ElementDefinition> structureElements) {
        String value = primitive.getValueAsString();
        fixedValues.put(id, value);

        // Create a new ElementDefinition for the primitive
        ElementDefinition edPrim = newElementDefinition(id, path, primitive);
        edPrim.setMin(1);
        edPrim.setMax("1");
        edPrim.setFixed(primitive);

        if (getElementByPath(structureElements, path) == null) {
            structureElements.add(edPrim);
        }
    }

    /**
     * Creates an ElementDefinition for a complex fixed type
     * (non-primitive case).
     */
    private void addElementDefinitionForFixedType(Type fixedType, String id, String path, List<ElementDefinition> structureElements) {
        ElementDefinition edParent = newElementDefinition(id, path, fixedType);
        edParent.setMin(1);
        edParent.setMax("1");
        edParent.setFixed(fixedType);

        if (getElementById(structureElements, id) == null) {
            structureElements.add(edParent);
        }
    }

    /**
     * Utility method for creating a fresh ElementDefinition
     * with basic properties set.
     */
    private ElementDefinition newElementDefinition(String id, String path, Type type) {
        ElementDefinition ed = new ElementDefinition();
        ed.setId(id);
        ed.setPath(path);

        ElementDefinition.TypeRefComponent typeRef = new ElementDefinition.TypeRefComponent();
        typeRef.setCode(type.fhirType());
        ed.addType(typeRef);

        return ed;
    }

    public ElementDefinition getElementByPath(List<ElementDefinition> structureElements, String path) {
        if (path == null) {
            return null;
        } else {
            Iterator var2 = structureElements.iterator();

            ElementDefinition ed;
            do {
                if (!var2.hasNext()) {
                    return null;
                }

                ed = (ElementDefinition)var2.next();
            } while(!path.equals(ed.getPath()) && !(path + "[x]").equals(ed.getPath()));

            return ed;
        }
    }

    public ElementDefinition getElementById(List<ElementDefinition> structureElements, String id) {
        if (id == null) {
            return null;
        } else {
            Iterator var2 = structureElements.iterator();

            ElementDefinition ed;
            do {
                if (!var2.hasNext()) {
                    return null;
                }

                ed = (ElementDefinition)var2.next();
            } while(!id.equals(ed.getId()) && !(id + "[x]").equals(ed.getId()));

            return ed;
        }
    }
}