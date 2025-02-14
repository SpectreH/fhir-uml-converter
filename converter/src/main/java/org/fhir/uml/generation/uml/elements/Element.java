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
    // private final ElementDefinition definition;
    private Boolean isMain;
    private Boolean isSliceHeader = false;
    private final Boolean hasFixedValue;
    private final String fixedValue;
    private final Integer commentId;
    private final Boolean choiseOfTypeHeader;
    private final Boolean choiseOfTypeElement;
    private String id;
    private String path;
    private Boolean hasSliceName;

    private Element(String name, String type, ElementVisability visability, Cardinality cardinality, String description, Boolean hasFixedValue, Integer commentId, Boolean choiseOfTypeHeader, Boolean choiseOfTypeElement, String fixedValue, String id, Boolean hasSliceName, Boolean isMain) {
        this.name = name;
        this.type = type;
        this.visability = visability;
        this.cardinality = cardinality;
        this.description = description;
        this.hasFixedValue = hasFixedValue;
        this.commentId = commentId;
        this.choiseOfTypeHeader = choiseOfTypeHeader;
        this.choiseOfTypeElement = choiseOfTypeElement;
        this.fixedValue = fixedValue;
        this.id = id;
        this.path = "";
        this.hasSliceName = hasSliceName;
        this.isMain = isMain;
    }

    public Boolean getChoiseOfTypeElement() {
        return choiseOfTypeElement;
    }

    public Boolean getHasSliceName() {
        return hasSliceName;
    }

    public void setHasSliceName(Boolean hasSliceName) {
        this.hasSliceName = hasSliceName;
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
        return this.id;
    }

    public String getParentName() {
        String[] parts = this.id.split("\\.|:");
        if (parts.length < 2) {
            return null;
        }
        return parts[parts.length - 2];
    }

    public String getParentId() {
        String input = this.id;

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
        String input = this.id;

        int lastColonIndex = input.lastIndexOf(':');

        return input.substring(0, lastColonIndex);
    }

    public Boolean isDataType() {
        return Character.isUpperCase(type.charAt(0));
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setId(String id) {
        this.id = id;
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

    // Builder Pattern for easy construction
    public static class Builder {
        private String name;
        private String type;
        private ElementVisability visability;
        private Cardinality cardinality;
        private String description;
        private Boolean hasFixedValue;
        private Integer commentId;
        private Boolean choiseOfTypeHeader;
        private Boolean choiseOfTypeElement;
        private String fixedValue;
        private String id;
        private boolean hasSliceName;
        private boolean isMain;


        public Builder() {
            name = "";
            type = "";
            description = "";
            hasFixedValue = false;
            commentId = null;
            choiseOfTypeHeader = false;
            choiseOfTypeElement = false;
            fixedValue = "";
            id = "";
            hasSliceName = false;
            isMain = false;
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

        public Builder hasFixedValue(Boolean hasFixedValue) {
            this.hasFixedValue = hasFixedValue;
            return this;
        }

        public Builder commentId(Integer commentId) {
            this.commentId = commentId;
            return this;
        }

        public Builder fixedValue(String fixedValue) {
            this.fixedValue = fixedValue;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder hasSliceName(Boolean hasSliceName) {
            this.hasSliceName = hasSliceName;
            return this;
        }

        public Builder isMain(Boolean isMain) {
            this.isMain = isMain;
            return this;
        }

        public Element build() {
            return new Element(name, type, visability, cardinality, description, hasFixedValue, commentId, choiseOfTypeHeader, choiseOfTypeElement, fixedValue, id, hasSliceName, isMain);
        }
    }

    public static String resolveType(ElementDefinition element) {
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
        if (this.isMain) {
            return "-";
        }

        if (this.visability == null) {
            return "+";
        }

        return this.visability.toSymbol();
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
//        if (commentId != null) {
//            commentReference.append("**(").append(commentId).append(")**");
//        }

        StringBuilder fixedValueSb = new StringBuilder();
        if (hasFixedValue) {
            fixedValueSb.append("= ").append("**").append(fixedValue).append("**");
        }

        return String.format("{field} %s %s : %s %s %s %s %s", matchVisability(), name, type, fixedValueSb.toString(), cardinalitySb.toString(), description, commentReference.toString());
    }
}
