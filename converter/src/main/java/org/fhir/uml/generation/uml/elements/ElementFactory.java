package org.fhir.uml.generation.uml.elements;

import org.fhir.uml.generation.uml.types.ElementVisability;
import org.fhir.uml.generation.uml.types.LegendPosition;
import org.hl7.fhir.r4.model.*;

import java.util.HashMap;
import java.util.Map;

/**
 * A non-static factory that knows how to create Elements.
 * It can hold onto both a Legend and a Map of fixedValues without
 * needing to rely on static fields.
 */
public class ElementFactory {
    private final Legend legend;
    private final Map<String, String> fixedValues;

    // Private constructor; use init(...) to instantiate
    private ElementFactory(Legend legend, Map<String, String> fixedValues) {
        this.legend = legend;
        this.fixedValues = fixedValues;
    }

    /**
     * Initialize the factory with a given Legend. Optionally you can
     * also initialize it with a brand-new or shared Map for fixed values.
     */
    public static ElementFactory init(Legend legend) {
        return new ElementFactory(legend, new HashMap<>());
    }

    /**
     * If you need to inject a custom Map of fixed values
     */
    public static ElementFactory init(Legend legend, Map<String, String> fixedValues) {
        return new ElementFactory(legend, fixedValues);
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

        String min = String.valueOf(elementDefinition.getMin());
        String max = elementDefinition.getMax();
        Cardinality extractedCardinality = new Cardinality(min, max);

        String extractedType = Element.resolveType(elementDefinition);

        if (elementDefinition.getType().size() > 1) {
            choisesOfTypesHeader = true;
        }

        if (elementDefinition.hasFixed()) {
            Type fixedType = elementDefinition.getFixed();
            if (fixedType instanceof StringType fixedString) {
                this.fixedValues.put(path, fixedString.getValue());
            } else if (fixedType.getClass() == UriType.class) {
                UriType fixedUriType = (UriType) fixedType;
                this.fixedValues.put(path, fixedUriType.getValue());
            } else if (fixedType.getClass() == CodeableConcept.class) {
                CodeableConcept fixedConcept = (CodeableConcept) fixedType;

                Coding coding = fixedConcept.getCoding().getFirst();
                if (coding.getCode() != null) {
                    this.fixedValues.put(path + ".code", coding.getCode());
                }
                if (coding.getDisplay() != null) {
                    this.fixedValues.put(path + ".display", coding.getDisplay());
                }
                if (coding.getSystem() != null) {
                    this.fixedValues.put(path + ".system", coding.getSystem());
                }
                if (coding.getVersion() != null) {
                    this.fixedValues.put(path + ".version", coding.getVersion());
                }
                if (coding.getUserSelectedElement() != null) {
                    this.fixedValues.put(path + ".userSelected", coding.getUserSelectedElement().toString());
                }
            } else if (fixedType.getClass() == Coding.class) {
                Coding coding = (Coding) fixedType;
                if (coding.getCode() != null) {
                    this.fixedValues.put(path + ".code", coding.getCode());
                }
                if (coding.getDisplay() != null) {
                    this.fixedValues.put(path + ".display", coding.getDisplay());
                }
                if (coding.getSystem() != null) {
                    this.fixedValues.put(path + ".system", coding.getSystem());
                }
                if (coding.getVersion() != null) {
                    this.fixedValues.put(path + ".version", coding.getVersion());
                }
                if (!coding.getUserSelectedElement().isEmpty()) {
                    this.fixedValues.put(path + ".userSelected", coding.getUserSelectedElement().getValue().toString());
                }
            }
        }

        commentId = legend.addComment(id, this.fixedValues.get(path), LegendPosition.CommentType.FIXED_VALUE);

        ElementVisability visability = ElementVisability.PUBLIC;
        if (commentId != null) {
            visability = ElementVisability.PROTECTED;
        }

        if (extractedType.contains("Reference") || extractedType.contains("canonical")) {
            visability = ElementVisability.PACKAGE_PRIVATE;
        }

        return new Element.Builder()
                .name(extractedName)
                .type(extractedType)
                .visability(visability)
                .cardinality(extractedCardinality)
                .description("")
                .definition(elementDefinition)
                .commentId(commentId)
                .fixedValue(commentId != null)
                .choiseOfTypeHeader(choisesOfTypesHeader)
                .choiseOfTypeElement(false)
                .build();
    }

    // Getter if you need to read or manipulate the map externally
    public Map<String, String> getFixedValues() {
        return fixedValues;
    }
}
