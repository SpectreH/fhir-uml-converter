package org.fhir.uml.generation.uml.elements;

import org.hl7.fhir.r4.model.ElementDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UMLClass {
    private String title;
    private String type;
    private String name;
    private List<Element> elements;
    private Boolean isSliceHeader = false;

    public UMLClass(String type, String name, Boolean isSliceHeader) {
        this.title = String.format("%s (%s)", type, name);
        this.type = type;
        this.name = name;
        this.elements = new ArrayList<>();
        this.isSliceHeader = isSliceHeader;
    }

    public Boolean isSliceHeader() {
        return this.isSliceHeader;
    }

    public void setSliceHeader(Boolean sliceHeader) {
        this.isSliceHeader = sliceHeader;
    }

    public String getTitle() {
        return title;
    }

    public void addElement(Element element) {
        this.elements.add(element);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private String matchGroup(Element element) {
        String type = element.getType();
        if (Character.isLowerCase(type.charAt(0))) {
            return "Primitive";
        }

        if (type.contains("Reference")) {
            return "Reference";
        }

        return "Data";
    }

    private String matchCustomClass() {
        StringBuilder sb = new StringBuilder();
        if (this.isSliceHeader) {
            return sb.append("<< (").append("S").append(",").append("#FF7700").append(") ").append("Slices").append(" >>").toString();
        }

        return sb.toString();
    }

    private Boolean isBackboneUML() {
        return this.type.contains("BackboneElement");
    }

    private String matchClassType() {
        if (isBackboneUML()) {
            return "struct";
        }

        return "class";
    }

    public String matchTitle() {
        if (this.isSliceHeader) {
            return String.format("Slices for %s", this.name);
        }

        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UMLClass umlClass = (UMLClass) o;
        return title != null && title.equals(umlClass.title);
    }

    @Override
    public int hashCode() {
        return title != null ? title.hashCode() : 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(matchClassType()).append(" \"").append(matchTitle()).append("\"").append(matchCustomClass()).append(" {\n");
        elements.forEach(e -> {
            if (!e.isRemoved()) {
                sb.append("\t").append(e).append("\n");
            }
        });

        sb.append("}\n");
        return sb.toString();
    }
}


