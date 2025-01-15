package org.fhir.uml.generation.uml;

import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.ElementDefinition.TypeRefComponent;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Type;
import org.hl7.fhir.r4.model.UrlType;

import java.util.List;
import java.util.stream.Collectors;

public class ElementVisualizer {
    private final String path;
    private final String type;
    private final String min;
    private final String max;
    private final String description;
    private final String sliceName;
    private final ElementDefinition.ElementDefinitionSlicingComponent slicing;
    private final Boolean isSummary;


    public ElementVisualizer(ElementDefinition element) {
        this.path = element.getPath();
        this.type = resolveType(element);
        this.min = String.valueOf(element.getMin());
        this.max = element.getMax();
        this.description = element.getDefinition();
        this.sliceName = resolveSliceName(element);
        this.slicing = element.getSlicing();
        this.isSummary = element.getIsSummary();
    }

    private String resolveType(ElementDefinition element) {
        if (!element.hasType()) {
            return "N/A";
        }

//        if (element.getType().size() != 1) {
//            System.out.println("NOT ONE"); // TODO Choice of Types
//        }

        return element.getType().stream()
                .map(this::resolveTypeComponent)
                .collect(Collectors.joining(", "));
    }

    private String resolveTypeComponent(TypeRefComponent type) {
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

    private String handleExtensionType(List<Extension> extensions) {
        Extension firstExtension = extensions.get(0); // Assuming the first extension
        if (firstExtension != null && firstExtension.getValue() instanceof UrlType) {
            return ((UrlType) firstExtension.getValue()).getValue();
        }
        return null;
    }

    private String handleReferenceType(TypeRefComponent type) {
        if (type.hasTargetProfile()) {
            String targetProfiles = type.getTargetProfile().stream()
                    .map(profile -> extractProfileName(profile.getValue()))
                    .collect(Collectors.joining(" | "));
            return String.format("Reference(%s)", targetProfiles);
        }
        return "Reference";
    }

    private String handleCanonicalType(TypeRefComponent type) {
        if (type.hasTargetProfile()) {
            String targetProfiles = type.getTargetProfile().stream()
                    .map(profile -> extractProfileName(profile.getValue()))
                    .collect(Collectors.joining(" | "));
            return String.format("canonical(%s)", targetProfiles);
        }
        return "canonical";
    }

    private String extractProfileName(String profileValue) {
        if (profileValue == null) {
            return "UnknownProfile";
        }
        return profileValue.substring(profileValue.lastIndexOf('/') + 1);
    }

    private String resolveSliceName(ElementDefinition element) {
        return element.getSliceName(); // Fallback to the slice name in the definition
    }

    public boolean isRemoved() {
        // Element is considered removed if min == 0 and max == "0"
        return "0".equals(max) && "0".equals(min);
    }

    public boolean isSliceHead() {
        return sliceName != null;
    }

    public boolean isSlicesFolder() {
        return !this.slicing.getDiscriminator().isEmpty() & this.isSummary;
    }

    public String visualizeAsAttribute() {
        // If the element is removed, mark it in the output
        if (isRemoved()) {
            return String.format("%s (REMOVED)", path);
        }

        if (this.isSliceHead()) {
            return String.format("%s:%s : %s [%s..%s]", this.path.substring(this.path.lastIndexOf('.') + 1), this.sliceName, type, min, max);
        }

        if (this.isSlicesFolder()) {
            return String.format("Slices for %s : %s [%s..%s]", this.path.substring(this.path.lastIndexOf('.') + 1), type, min, max);
        }

        return String.format("%s : %s [%s..%s]", path, type, min, max);
    }

    public String getPath() {
        return path;
    }
}
