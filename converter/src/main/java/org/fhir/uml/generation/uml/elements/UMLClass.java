package org.fhir.uml.generation.uml.elements;

import org.fhir.uml.generation.uml.types.CustomClassType;
import org.fhir.uml.generation.uml.utils.Config;
import org.fhir.uml.generation.uml.utils.Utils;

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
    private boolean parentElementIsRemoved;
    private Config config = Config.getInstance();

    public UMLClass(String type, String name, Element mainElement, Element parentElement, boolean parentElementIsRemoved) {
        this.type = type;
        this.name = name;
        this.elements = new ArrayList<>();
        this.mainElement = mainElement;
        this.parentElement = parentElement;
        this.mainClass = false;
        this.parentElementIsRemoved = parentElementIsRemoved;
    }

    public boolean isParentElementIsRemoved() {
        return parentElementIsRemoved;
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
        return this.mainElement.isChoiceOfTypeHeader();
    }

    public String getTitle() {
        if (isChoiseOfTypeHeader()) {
            return Utils.capitalize(name).replace("[x]", "");
        }

        if (this.isMainClass()) {
            return String.format("%s (%s)", type, name);
        }

        return Utils.capitalize(name);
    }

    public void addElement(Element element) {
        this.elements.add(element);
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
        String title = this.getTitle();
//        String name = "TEST";
//
//        if (this.parentElement != null) {
//            name = parentElement.getName();
//        }

        if (this.isSliceHeader()) {
            title = String.format("Slices for %s (%s)", parentElement.getName(), Utils.capitalize(name).replace("[x]", ""));
        }

        return wrapDifferential(title);
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

    private String wrapDifferential(String value) {
        if (config.getView().equals("differential")) {
            return "black('" + value + "')";
        }

        return value;
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

        List<Element> copyElements = new ArrayList<>(elements);

        Map<String, List<Element>> groupMapper = new LinkedHashMap<>();

        Map<String, List<Element>> predefinedGroupMapper = new LinkedHashMap<>();

        Iterator<Element> iterator = copyElements.iterator();
        while (iterator.hasNext()) {
            Element element = iterator.next();
            String group = element.getGroup();

            // If we detect "predefined group" logic here, store these in the temporary map
            if (group != null && !group.isEmpty() && element.getHasSliceName()) {
                predefinedGroupMapper
                        .computeIfAbsent(group, k -> new ArrayList<>())
                        .add(element);

                // Remove from copyElements so they aren't handled again below
                iterator.remove();
            }
        }

        if (isSliceHeader()) {
            final String mainGroup = "Content/Rules for all slices";
            final String slicesGroup = "Slices";

            Map<Boolean, List<Element>> partitionedMap = copyElements.stream()
                    .collect(Collectors.partitioningBy(Element::getHasSliceName));

            groupMapper.put(mainGroup, partitionedMap.getOrDefault(false, Collections.emptyList()));
            groupMapper.put(slicesGroup, partitionedMap.getOrDefault(true, Collections.emptyList()));

        } else if (isChoiseOfTypeHeader()) {
            final String mainGroup = "Content/Rules for all types";
            final String choicesGroup = "Types";

            Map<Boolean, List<Element>> partitionedMap = copyElements.stream()
                    .collect(Collectors.partitioningBy(Element::getChoiceOfTypeElement));

            groupMapper.put(mainGroup, partitionedMap.getOrDefault(false, Collections.emptyList()));
            groupMapper.put(choicesGroup, partitionedMap.getOrDefault(true, Collections.emptyList()));

        } else {
            // Fallback/no special grouping
            groupMapper.put("", new ArrayList<>(copyElements));
        }

        for (Map.Entry<String, List<Element>> entry : predefinedGroupMapper.entrySet()) {
            groupMapper.put(entry.getKey(), entry.getValue());
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
                    .filter(e -> (e.isChoiceOfTypeElement() || !e.isMain()))
                    .filter(e -> !config.isHideRemovedObjects() || !e.isRemoved())
                    .forEach(e -> sb.append("\t").append(e).append("\n"));
        }

        sb.append("}\n");
        return sb.toString();
    }
}


