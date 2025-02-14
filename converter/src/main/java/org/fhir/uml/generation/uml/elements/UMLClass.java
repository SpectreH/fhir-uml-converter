package org.fhir.uml.generation.uml.elements;

import org.fhir.uml.generation.uml.types.CustomClassType;

import java.util.*;
import java.util.stream.Collectors;

public class UMLClass {
    private boolean mainClass;
    private String title;
    private String type;
    private String name;
    private List<Element> elements;
    private Element mainElement;
    private Element parentElement;
    private CustomClassType customClassType;

    public UMLClass(String type, String name, Element mainElement, Element parentElement) {
        this.title = String.format("%s (%s)", type, name);
        this.type = type;
        this.name = name;
        this.elements = new ArrayList<>();
        this.mainElement = mainElement;
        this.parentElement = parentElement;
        this.mainClass = false;
    }

    public boolean isMainClass() {
        return mainClass;
    }

    public void setMainClass(boolean mainClass) {
        this.mainClass = mainClass;
    }

    public List<Element> getElements() {
        return elements;
    }

    public void updateTitle() {
        this.title = String.format("%s (%s)", type, name);
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Element getMainElement() {
        return mainElement;
    }

    public Element getParentElement() {
        return parentElement;
    }

    public void setParentElement(Element parentElement) {
        this.parentElement = parentElement;
    }

    public void setMainElement(Element mainElement) {
        this.mainElement = mainElement;
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

    public void setType(String type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CustomClassType getCustomClassType() {
        return customClassType;
    }

    public void setCustomClassType(CustomClassType customClassType) {
        this.customClassType = customClassType;
    }

    public Element findElementByName(String elementName) {
        for (Element element : this.elements) {
            if (element.getName().equals(elementName)) {
                return element;
            }
        }
        return null;
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
            return CustomClassType.SLICES.toString();
        }

        if (this.isChoiseOfTypeHeader()) {
            return CustomClassType.CHOICE_OF_TYPES.toString();
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

        // Start your UML/class-like definition
        sb.append(matchClassType())
                .append(" \"")
                .append(matchTitle())
                .append("\"")
                .append(matchCustomClass())
                .append(" {\n");

        // This map will hold groups -> list of elements
        Map<String, List<Element>> groupMapper = new LinkedHashMap<>();

        if (isSliceHeader()) {
            final String mainGroup = "Content/Rules for all slices";
            final String slicesGroup = "Slices";
            Map<Boolean, List<Element>> partitionedMap = elements.stream()
                    .collect(Collectors.partitioningBy(Element::getHasSliceName));

            groupMapper.put(mainGroup, partitionedMap.getOrDefault(false, Collections.emptyList()));
            groupMapper.put(slicesGroup, partitionedMap.getOrDefault(true, Collections.emptyList()));
        } else if (isChoiseOfTypeHeader()) {
            final String mainGroup = "Content/Rules for all slices";
            final String choisesGroup = "Types";

            Map<Boolean, List<Element>> partitionedMap = elements.stream()
                    .collect(Collectors.partitioningBy(Element::getChoiseOfTypeElement));

            groupMapper.put(mainGroup, partitionedMap.getOrDefault(false, Collections.emptyList()));
            groupMapper.put(choisesGroup, partitionedMap.getOrDefault(true, Collections.emptyList()));
        } else {
            groupMapper.put("", new ArrayList<>(elements));
        }

        // Now use groupMapper to print out each group and the corresponding elements
        for (Map.Entry<String, List<Element>> entry : groupMapper.entrySet()) {
            String groupName = entry.getKey();
            List<Element> groupElements = entry.getValue();

            if (groupElements.isEmpty()) {
                continue;
            }

            if (!groupName.isEmpty()) {
                // Print the group name
                sb.append("\t").append("--").append(groupName).append("--").append("\n");
            }

            // Print each element that’s not removed or is choice-of-type
            groupElements.stream()
                    .filter(e -> e.isChoiseOfTypeElement() || !e.isRemoved() && !e.isMain())
                    .forEach(e -> sb.append("\t").append(e).append("\n"));
        }

        sb.append("}\n");
        return sb.toString();
    }
}


