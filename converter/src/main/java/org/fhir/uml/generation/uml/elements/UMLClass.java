package org.fhir.uml.generation.uml.elements;

import java.util.ArrayList;
import java.util.List;

public class UMLClass {
    private String title;
    private String type;
    private String name;
    private List<Element> elements;
    private Element mainElement;
    private Element parentElement;

    public UMLClass(String type, String name, Element mainElement, Element parentElement) {
        this.title = String.format("%s (%s)", type, name);
        this.type = type;
        this.name = name;
        this.elements = new ArrayList<>();
        this.mainElement = mainElement;
        this.parentElement = parentElement;
    }

    public Element getMainElement() {
        return mainElement;
    }

    public Element getParentElement() {
        return parentElement;
    }

    public Boolean isSliceHeader() {
        return this.mainElement.isSliceHeader();
    }

    public Boolean isChoiseOfTypeHeader() {
        return this.mainElement.isChoiseOfTypeHeader();
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
        if (this.isSliceHeader()) {
            return sb.append("<< (").append("S").append(",").append("#FF7700").append(") ").append("Slices").append(" >>").toString();
        }

        if (this.isChoiseOfTypeHeader()) {
            return sb.append("<< (").append("C").append(",").append("#1892ba").append(") ").append("Choise of Types").append(" >>").toString();
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
        if (this.isSliceHeader()) {
            return String.format("Slices for %s (%s)", this.name, this.parentElement.getName());
        }

        if (this.isChoiseOfTypeHeader()) {
            return String.format("%s", this.name);
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
            if (e.isChoiseOfTypeElement() || !e.isRemoved()) {
                sb.append("\t").append(e).append("\n");
            }
        });

        sb.append("}\n");
        return sb.toString();
    }
}


