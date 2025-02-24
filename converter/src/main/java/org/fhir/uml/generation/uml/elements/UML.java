package org.fhir.uml.generation.uml.elements;

import org.fhir.uml.generation.uml.utils.Config;

import java.util.LinkedList;
import java.util.List;

public class UML {
    private final LinkedList<UMLClass> classes;
    private final LinkedList<Relation> relations;
    private UMLClass mainClass;
    private Legend legend;
    private final Config config = Config.getInstance();

    public UML() {
        this.classes = new LinkedList<>();
        this.relations = new LinkedList<>();
    }

    public void addClass(UMLClass umlClass) {
        if (umlClass != null) {
            this.classes.add(umlClass);

            if (umlClass.isMainClass()) {
                mainClass = umlClass;
            }
        }
    }

    public void addRelation(Relation relation) {
        if (relation != null) {
            this.relations.add(relation);
        }
    }

    public UMLClass findClassByTitle(String title) {
        for (UMLClass umlClass : classes) {
            if (umlClass.getTitle().equals(title)) {
                return umlClass;
            }
        }
        return null;
    }

    public UMLClass findClassByElement(Element element) {
        for (UMLClass umlClass : classes) {
            if (umlClass.getMainElement().equals(element)) {
                return umlClass;
            }
        }
        return null;
    }

    public UMLClass getMainClass() {
        return mainClass;
    }

    public void setMainClass(UMLClass mainClass) {
        this.mainClass = mainClass;
    }

    public List<UMLClass> getClasses() {
        return this.classes;
    }

    public List<Relation> getRelations() {
        return this.relations;
    }

    public void setLegend(Legend legend) {
        this.legend = legend;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("@startuml\n");
        sb.append("hide empty members\n");
        sb.append("skinparam wrapwidth 500\n");
        sb.append("left to right direction\n");

        sb.append("skinparam classStereotypeFontColor black\n");

        if (config.isDifferential()) {
            sb.append("skinparam classAttributeFontColor #808080\n");
        }

        sb.append("!function bold($value)\n");
        sb.append("!return \"<b>\" + $value + \"</b>\"\n");
        sb.append("!endfunction\n");

        sb.append("!function black($value)\n");
        sb.append("!return \"<color:Black>\" + $value + \"</color>\"\n");
        sb.append("!endfunction\n");

        sb.append("!function strikethrough($value)\n");
        sb.append("!return \"<s>\" + $value + \"</s>\"\n");
        sb.append("!endfunction\n");

        sb.append("\n");

        for (UMLClass umlClass : this.classes) {
            if (config.isHideRemovedObjects() && umlClass.isParentElementIsRemoved()) {
                continue;
            }
            sb.append(umlClass);
        }

        for (Relation relation : this.relations) {
            if (config.isHideRemovedObjects() & relation.getCardinality().isRemoved()) {
                continue;
            }
            sb.append(relation);
        }

        if (this.legend != null) {
            sb.append(this.legend);
        }

        sb.append("@enduml");
        return sb.toString();
    }
}
