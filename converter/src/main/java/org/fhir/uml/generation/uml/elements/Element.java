package org.fhir.uml.generation.uml.elements;

import org.fhir.uml.generation.uml.types.ElementVisability;
import org.fhir.uml.generation.uml.types.LegendPosition;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.utils.SnomedExpressions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Element {
    private final String name;
    private String type;
    private final ElementVisability visability;
    private final Cardinality cardinality;
    private final String description;
    private final ElementDefinition definition;
    private Boolean isMain = false;
    private Boolean isSliceHeader = false;
    private final Boolean fixedValue;
    private final Integer commentId;
    private final Boolean choiseOfTypeHeader;
    private final Boolean choiseOfTypeElement;

    private Element(String name, String type, ElementVisability visability, Cardinality cardinality, String description, ElementDefinition definition, Boolean fixedValue, Integer commentId, Boolean choiseOfTypeHeader, Boolean choiseOfTypeElement) {
        this.name = name;
        this.type = type;
        this.visability = visability;
        this.cardinality = cardinality;
        this.description = description;
        this.definition = definition;
        this.fixedValue = fixedValue;
        this.commentId = commentId;
        this.choiseOfTypeHeader = choiseOfTypeHeader;
        this.choiseOfTypeElement = choiseOfTypeElement;
    }

    public Boolean isChoiseOfTypeHeader() {
        return choiseOfTypeHeader;
    }

    public Boolean isChoiseOfTypeElement() {
        return choiseOfTypeElement;
    }

    public Boolean isSliceHeader() {
        return this.isSliceHeader;
    }

    public void setSliceHeader(Boolean sliceHeader) {
        this.isSliceHeader = sliceHeader;
    }

    public String getElementId() {
        return this.definition.getId();
    }

    public String getParentName() {
        String[] parts = this.definition.getId().split("\\.|:");
        if (parts.length < 2) {
            return null;
        }
        return parts[parts.length - 2];
    }

    public String getParentId() {
        String input = this.definition.getId();

        int lastDotIndex = input.lastIndexOf('.');
        int lastColonIndex = input.lastIndexOf(':');

        // Determine the separator to use for splitting
        int splitIndex = Math.max(lastDotIndex, lastColonIndex);

        // If no valid separator is found, return the input as-is
        if (splitIndex == -1) {
            return input;
        }

        // Return the substring excluding the last segment
        return input.substring(0, splitIndex);
    }

    public String getSliceParentId() {
        String input = this.definition.getId();

        int lastColonIndex = input.lastIndexOf(':');

        return input.substring(0, lastColonIndex);
    }

    public Boolean isDataType() {
        return Character.isUpperCase(type.charAt(0));
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setIsMain(Boolean isMain) {
        this.isMain = isMain;
    }

    public Boolean isMain() {
        return isMain;
    }

    // Public getters so we can retrieve this info later
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public ElementVisability getVisability() {
        return visability;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    public String getDescription() {
        return description;
    }

    public ElementDefinition getDefinition() {
        return definition;
    }

    // Builder Pattern for easy construction
    public static class Builder {
        private String name;
        private String type;
        private ElementVisability visability;
        private Cardinality cardinality;
        private String description;
        private ElementDefinition definition;
        private Boolean fixedValue;
        private Integer commentId;
        private Boolean choiseOfTypeHeader;
        private Boolean choiseOfTypeElement;


        public Builder() {
            name = "";
            type = "";
            description = "";
            fixedValue = false;
            commentId = null;
            choiseOfTypeHeader = false;
            choiseOfTypeElement = false;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder choiseOfTypeHeader(Boolean choiseOfTypeHeader) {
            this.choiseOfTypeHeader = choiseOfTypeHeader;
            return this;
        }

        public Builder choiseOfTypeElement(Boolean choiseOfTypeElement) {
            this.choiseOfTypeElement = choiseOfTypeElement;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder visability(ElementVisability visability) {
            this.visability = visability;
            return this;
        }

        public Builder cardinality(Cardinality cardinality) {
            this.cardinality = cardinality;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder definition(ElementDefinition definition) {
            this.definition = definition;
            return this;
        }

        public Builder fixedValue(Boolean fixedValue) {
            this.fixedValue = fixedValue;
            return this;
        }

        public Builder commentId(Integer commentId) {
            this.commentId = commentId;
            return this;
        }

        public Element build() {
            return new Element(name, type, visability, cardinality, description, definition, fixedValue, commentId, choiseOfTypeHeader, choiseOfTypeElement);
        }
    }

    static String resolveType(ElementDefinition element) {
        if (!element.hasType()) {
            return "N/A";
        }

        return element.getType().stream()
                .map(Element::resolveTypeComponent)
                .collect(Collectors.joining(", "));
    }

    private static String resolveTypeComponent(ElementDefinition.TypeRefComponent type) {
        if (type == null) {
            return "Unknown";
        }

        if (!type.getExtension().isEmpty()) {
            String extensionType = handleExtensionType(type.getExtension());
            if (extensionType != null) {
                return extensionType;
            }
        }

        String baseType = type.getCode();

        if ("Reference".equals(baseType)) {
            return handleReferenceType(type);
        }

        if ("canonical".equals(baseType)) {
            return handleCanonicalType(type);
        }

        return baseType != null ? baseType : "N/A";
    }


    private static String handleExtensionType(List<Extension> extensions) {
        Extension firstExtension = extensions.get(0); // Assuming the first extension
        if (firstExtension != null && firstExtension.getValue() instanceof UrlType) {
            return ((UrlType) firstExtension.getValue()).getValue();
        }
        return null;
    }

    private static String handleReferenceType(ElementDefinition.TypeRefComponent type) {
        if (type.hasTargetProfile()) {
            String targetProfiles = type.getTargetProfile().stream()
                    .map(profile -> extractProfileName(profile.getValue()))
                    .collect(Collectors.joining(" | "));
            return String.format("Reference(%s)", targetProfiles);
        }
        return "Reference";
    }

    private static String handleCanonicalType(ElementDefinition.TypeRefComponent type) {
        if (type.hasTargetProfile()) {
            String targetProfiles = type.getTargetProfile().stream()
                    .map(profile -> extractProfileName(profile.getValue()))
                    .collect(Collectors.joining(" | "));
            return String.format("canonical(%s)", targetProfiles);
        }
        return "canonical";
    }

    private static String extractProfileName(String profileValue) {
        if (profileValue == null) {
            return "UnknownProfile";
        }
        return profileValue.substring(profileValue.lastIndexOf('/') + 1);
    }

    private String resolveSliceName(ElementDefinition element) {
        return element.getSliceName(); // Fallback to the slice name in the definition
    }

    public String matchVisability() {
        if (this.visability == null) {
            return "+";
        }

        return switch (this.visability) {
            case PRIVATE -> "-";
            case PROTECTED -> "#";
            case PACKAGE_PRIVATE -> "~";
            default -> "+";
        };
    }

    public boolean isRemoved() {
        return cardinality.isRemoved();
    }

    @Override
    public String toString() {
        StringBuilder cardinalitySb = new StringBuilder();
        if (cardinality != null) {
            cardinalitySb.append(cardinality.toString());
        }

        StringBuilder commentReference = new StringBuilder();
        if (commentId != null) {
            commentReference.append("**(").append(commentId).append(")**");
        }

        return String.format("{field} %s %s : %s %s %s %s", matchVisability(), name, type, cardinalitySb.toString(), description, commentReference.toString());
    }
}
