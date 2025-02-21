package org.fhir.uml.generation.uml.elements;

import org.fhir.uml.generation.uml.types.ElementVisability; // If you control this class name, consider renaming to ElementVisibility
import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.UrlType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a UML Element with FHIR-specific properties (e.g., cardinalities, slice headers, etc.).
 */
public class Element {

    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    private final String name;
    private String type;
    private final ElementVisability visibility;
    private final Cardinality cardinality;
    private final String description;

    private Boolean isMain;
    private Boolean isSliceHeader = false;
    private final Boolean hasFixedValue;
    private final String fixedValue;
    private final Integer commentId;
    private final Boolean choiceOfTypeHeader;
    private final Boolean choiceOfTypeElement;
    private String id;
    private String path;
    private Boolean hasSliceName;

    // ---------------------------------------------------------------------------------------------
    // Private Constructor
    // ---------------------------------------------------------------------------------------------
    private Element(String name,
                    String type,
                    ElementVisability visibility,
                    Cardinality cardinality,
                    String description,
                    Boolean hasFixedValue,
                    Integer commentId,
                    Boolean choiceOfTypeHeader,
                    Boolean choiceOfTypeElement,
                    String fixedValue,
                    String id,
                    Boolean hasSliceName,
                    Boolean isMain) {

        this.name = name;
        this.type = type;
        this.visibility = visibility;
        this.cardinality = cardinality;
        this.description = description;
        this.hasFixedValue = hasFixedValue;
        this.commentId = commentId;
        this.choiceOfTypeHeader = choiceOfTypeHeader;
        this.choiceOfTypeElement = choiceOfTypeElement;
        this.fixedValue = fixedValue;
        this.id = id;
        this.path = "";
        this.hasSliceName = hasSliceName;
        this.isMain = isMain;
    }

    // ---------------------------------------------------------------------------------------------
    // Static Utility Methods
    // ---------------------------------------------------------------------------------------------

    /**
     * Resolves the type(s) of an ElementDefinition into a comma-separated string.
     * If no types exist, returns "N/A".
     */
    public static String resolveType(ElementDefinition element) {
        if (!element.hasType()) {
            return "N/A";
        }
        return element.getType().stream()
                .map(Element::resolveTypeComponent)
                .collect(Collectors.joining(", "));
    }

    /**
     * Helper method to resolve a single TypeRefComponent into a string representation.
     */
    private static String resolveTypeComponent(ElementDefinition.TypeRefComponent type) {
        if (type == null) {
            return "Unknown";
        }

        // Check for extension-based type override
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

    /**
     * Extracts a type override from the first extension if it exists.
     */
    private static String handleExtensionType(List<Extension> extensions) {
        Extension firstExtension = extensions.get(0);
        if (firstExtension != null && firstExtension.getValue() instanceof UrlType) {
            return ((UrlType) firstExtension.getValue()).getValue();
        }
        return null;
    }

    /**
     * Formats a Reference type string, including its possible target profiles.
     */
    private static String handleReferenceType(ElementDefinition.TypeRefComponent type) {
        if (type.hasTargetProfile()) {
            String targetProfiles = type.getTargetProfile().stream()
                    .map(profile -> extractProfileName(profile.getValue()))
                    .collect(Collectors.joining(" | "));
            return String.format("Reference(%s)", targetProfiles);
        }
        return "Reference";
    }

    /**
     * Formats a canonical type string, including its possible target profiles.
     */
    private static String handleCanonicalType(ElementDefinition.TypeRefComponent type) {
        if (type.hasTargetProfile()) {
            String targetProfiles = type.getTargetProfile().stream()
                    .map(profile -> extractProfileName(profile.getValue()))
                    .collect(Collectors.joining(" | "));
            return String.format("canonical(%s)", targetProfiles);
        }
        return "canonical";
    }

    /**
     * Extracts the trailing portion of a URL (after the last slash) as the profile name.
     */
    private static String extractProfileName(String profileValue) {
        if (profileValue == null) {
            return "UnknownProfile";
        }
        return profileValue.substring(profileValue.lastIndexOf('/') + 1);
    }

    // ---------------------------------------------------------------------------------------------
    // Builder
    // ---------------------------------------------------------------------------------------------

    public static class Builder {
        private String name;
        private String type;
        private ElementVisability visibility;
        private Cardinality cardinality;
        private String description;
        private Boolean hasFixedValue;
        private Integer commentId;
        private Boolean choiceOfTypeHeader;
        private Boolean choiceOfTypeElement;
        private String fixedValue;
        private String id;
        private boolean hasSliceName;
        private boolean isMain;

        public Builder() {
            // Default initialization
            this.name = "";
            this.type = "";
            this.description = "";
            this.hasFixedValue = false;
            this.commentId = null;
            this.choiceOfTypeHeader = false;
            this.choiceOfTypeElement = false;
            this.fixedValue = "";
            this.id = "";
            this.hasSliceName = false;
            this.isMain = false;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder visibility(ElementVisability visibility) {
            this.visibility = visibility;
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

        public Builder choiceOfTypeHeader(Boolean choiceOfTypeHeader) {
            this.choiceOfTypeHeader = choiceOfTypeHeader;
            return this;
        }

        public Builder choiceOfTypeElement(Boolean choiceOfTypeElement) {
            this.choiceOfTypeElement = choiceOfTypeElement;
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
            return new Element(
                    this.name,
                    this.type,
                    this.visibility,
                    this.cardinality,
                    this.description,
                    this.hasFixedValue,
                    this.commentId,
                    this.choiceOfTypeHeader,
                    this.choiceOfTypeElement,
                    this.fixedValue,
                    this.id,
                    this.hasSliceName,
                    this.isMain
            );
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Getters and Setters
    // ---------------------------------------------------------------------------------------------
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public ElementVisability getVisibility() {
        return visibility;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getChoiceOfTypeElement() {
        return choiceOfTypeElement;
    }

    public Boolean isChoiceOfTypeHeader() {
        return choiceOfTypeHeader;
    }

    public Boolean isChoiceOfTypeElement() {
        return choiceOfTypeElement;
    }

    public Boolean getHasSliceName() {
        return hasSliceName;
    }

    public void setHasSliceName(Boolean hasSliceName) {
        this.hasSliceName = hasSliceName;
    }

    public Boolean isSliceHeader() {
        return isSliceHeader;
    }

    public void setSliceHeader(Boolean sliceHeader) {
        this.isSliceHeader = sliceHeader;
    }

    public String getElementId() {
        return this.id;
    }

    public Boolean isMain() {
        return isMain;
    }

    public void setIsMain(Boolean isMain) {
        this.isMain = isMain;
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

    // ---------------------------------------------------------------------------------------------
    // Additional Utilities
    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the element's parent name by splitting the ID on '.' or ':'.
     * Example: "Patient.identifier" → parent name is "Patient".
     */
    public String getParentName() {
        String[] parts = this.id.split("\\.|:");
        if (parts.length < 2) {
            return null;
        }
        return parts[parts.length - 2];
    }

    /**
     * Returns the parent ID by trimming everything after the last '.' or ':'.
     * Example: "Patient.identifier.system" → "Patient.identifier"
     */
    public String getParentId() {
        int lastDotIndex = id.lastIndexOf('.');
        int lastColonIndex = id.lastIndexOf(':');
        int splitIndex = Math.max(lastDotIndex, lastColonIndex);

        if (splitIndex == -1) {
            return id;
        }
        return id.substring(0, splitIndex);
    }

    /**
     * Returns the slice's parent ID by trimming everything after the last ':'.
     */
    public String getSliceParentId() {
        int lastColonIndex = id.lastIndexOf(':');
        if (lastColonIndex == -1) {
            return id;
        }
        return id.substring(0, lastColonIndex);
    }

    /**
     * Determines if this element is a data type (i.e., if the type starts with an uppercase letter).
     */
    public Boolean isDataType() {
        return type != null
                && !type.isEmpty()
                && Character.isUpperCase(type.charAt(0));
    }

    /**
     * Matches visibility to its UML representation symbol (e.g., "+" for public).
     */
    public String matchVisibilitySymbol() {
        if (Boolean.TRUE.equals(this.isMain)) {
            return "-";
        }
        if (this.visibility == null) {
            return "+";
        }
        return this.visibility.toSymbol();
    }

    /**
     * Determines if the cardinality indicates the element is removed.
     */
    public boolean isRemoved() {
        return cardinality != null && cardinality.isRemoved();
    }

    // ---------------------------------------------------------------------------------------------
    // Overrides
    // ---------------------------------------------------------------------------------------------

    @Override
    public String toString() {
        StringBuilder cardinalitySb = new StringBuilder();
        if (cardinality != null) {
            cardinalitySb.append(cardinality.toString());
        }

        StringBuilder fixedValueSb = new StringBuilder();
        if (Boolean.TRUE.equals(hasFixedValue)) {
            fixedValueSb.append("= **").append(fixedValue).append("**");
        }

        /*
          Example Format:
          {field} + name : type = **fixedValue** [cardinality] description
        */
        return String.format(
                "{field} %s %s : %s %s %s %s",
                matchVisibilitySymbol(),
                name,
                type,
                fixedValueSb,
                cardinalitySb,
                description
        );
    }
}
