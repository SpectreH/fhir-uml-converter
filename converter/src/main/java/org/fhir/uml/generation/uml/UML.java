package org.fhir.uml.generation.uml;

import org.fhir.uml.generation.uml.elements.Cardinality;
import org.fhir.uml.generation.uml.elements.Legend;
import org.fhir.uml.generation.uml.elements.Relation;
import org.fhir.uml.generation.uml.elements.UMLClass;

import java.util.LinkedList;
import java.util.List;

public class UML {
    private final LinkedList<UMLClass> classes;
    private final LinkedList<Relation> relations;
    private Legend legend;

    public UML() {
        this.classes = new LinkedList<>();
        this.relations = new LinkedList<>();
    }

    public void addClass(UMLClass umlClass) {
        if (umlClass != null) {
            this.classes.add(umlClass);
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

        sb.append("\n");

        for (UMLClass umlClass : this.classes) {
            sb.append(umlClass.toString());
        }

        for (Relation relation : this.relations) {
            if (!relation.getCardinality().isRemoved()) {
                sb.append(relation.toString());
            }

        }

        sb.append(this.legend.toString());

        sb.append("@enduml");
        return sb.toString();
    }
}
