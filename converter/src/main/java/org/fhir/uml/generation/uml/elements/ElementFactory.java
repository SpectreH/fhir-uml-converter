package org.fhir.uml.generation.uml.elements;

import org.fhir.uml.generation.uml.types.ElementVisability;
import org.fhir.uml.generation.uml.types.LegendPosition;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.r4.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A non-static factory that knows how to create Elements.
 * It can hold onto both a Legend and a Map of fixedValues without
 * needing to rely on static fields.
 */
public class ElementFactory {
    private final Map<String, String> fixedValues;

    // Private constructor; use init(...) to instantiate
    private ElementFactory(Map<String, String> fixedValues) {
        this.fixedValues = fixedValues;
    }

    /**
     * Initialize the factory with a given Legend. Optionally you can
     * also initialize it with a brand-new or shared Map for fixed values.
     */
    public static ElementFactory init() {
        return new ElementFactory(new HashMap<>());
    }

    /**
     * If you need to inject a custom Map of fixed values
     */
    public static ElementFactory init(Map<String, String> fixedValues) {
        return new ElementFactory(fixedValues);
    }

    /**
     * Factory method that recreates the logic from
     * Element.fromElementDefinition(elementDefinition, legend).
     *
     * Instead of referencing static fields in Element, we can now access
     * `this.fixedValues` or `this.legend`.
     */
    public Element fromElementDefinition(ElementDefinition elementDefinition) {
        boolean choisesOfTypesHeader = false;
        Integer commentId = null;
        String path = elementDefinition.getPath();
        String id = elementDefinition.getId();
        String[] parts = id.split("\\.|:");
        String extractedName = (parts.length == 0) ? null : parts[parts.length - 1];
        String fixedValue = "";
        boolean hasFixedValue = false;

        String min = String.valueOf(elementDefinition.getMin());
        String max = elementDefinition.getMax();
        Cardinality extractedCardinality = new Cardinality(min, max);

        String extractedType = Element.resolveType(elementDefinition);

        if (elementDefinition.getType().size() > 1 && !(extractedType.contains("Reference") || extractedType.contains("canonical"))) {
            choisesOfTypesHeader = true;
        }

        if (elementDefinition.hasFixed()) {
            parseFixedValues(elementDefinition.getFixed(), path);
        }

        ElementVisability visability = ElementVisability.PUBLIC;
        if (extractedType.contains("Reference") || extractedType.contains("canonical")) {
            visability = ElementVisability.PACKAGE_PRIVATE;
        }

        if (this.fixedValues.containsKey(path)) {
            fixedValue = fixedValues.get(path);
            hasFixedValue = true;
            visability = ElementVisability.PROTECTED;
        }

        return new Element.Builder()
                .name(extractedName)
                .type(extractedType)
                .visability(visability)
                .cardinality(extractedCardinality)
                .description("")
                .hasFixedValue(hasFixedValue)
                .fixedValue(fixedValue)
                .choiseOfTypeHeader(choisesOfTypesHeader)
                .choiseOfTypeElement(false)
                .id(elementDefinition.getId())
                .hasSliceName(elementDefinition.getSliceName() != null && !elementDefinition.getSliceName().isEmpty())
                .build();
    }

    private void parseFixedValues(Type fixedType, String path) {
        if (fixedType instanceof PrimitiveType) {
            String value = ((PrimitiveType<?>) fixedType).getValueAsString();
            fixedValues.put(path, value);
            System.out.println(path + ": " + value);
            return;
        }

        List<Property> children = fixedType.children();
        for (Property childProp : children) {
            String childName = childProp.getName();
            List<Base> values = childProp.getValues();

            for (int i = 0; i < values.size(); i++) {
                IBase childValue = values.get(i);

                String indexedPath = path + "." + childName;
                if (values.size() > 1) {
                    indexedPath += "[" + i + "]";
                }

                if (childValue instanceof Type) {
                    parseFixedValues((Type) childValue, indexedPath);
                }
            }
        }
    }

    // Getter if you need to read or manipulate the map externally
    public Map<String, String> getFixedValues() {
        return fixedValues;
    }
}
