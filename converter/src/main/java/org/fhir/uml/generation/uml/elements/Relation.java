package org.fhir.uml.generation.uml.elements;

import org.fhir.uml.generation.uml.elements.Cardinality;
import org.fhir.uml.generation.uml.elements.UMLClass;
import org.fhir.uml.generation.uml.types.RelationShipType;

public class Relation {
    private UMLClass from;
    private UMLClass to;
    private RelationShipType type;
    private String relationLabel;
    private Cardinality cardinality;

    private Relation(UMLClass from, UMLClass to, RelationShipType type, String relationLabel, Cardinality cardinality) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.relationLabel = relationLabel;
        this.cardinality = cardinality;
    }

    // Getters
    public UMLClass getFrom() {
        return from;
    }

    public UMLClass getTo() {
        return to;
    }

    public RelationShipType getType() {
        return type;
    }

    public String getRelationLabel() {
        return relationLabel;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }


    // Builder Pattern
    public static class Builder {
        private UMLClass from;
        private UMLClass to;
        private RelationShipType type;
        private String relationLabel;
        private Cardinality cardinality;

        public Builder() {}

        public Builder from(UMLClass from) {
            this.from = from;
            return this;
        }

        public Builder to(UMLClass to) {
            this.to = to;
            return this;
        }

        public Builder type(RelationShipType type) {
            this.type = type;
            return this;
        }

        public Builder relationLabel(String relationLabel) {
            this.relationLabel = relationLabel;
            return this;
        }

        public Builder cardinality(Cardinality cardinality) {
            this.cardinality = cardinality;
            return this;
        }

        public Relation build() {
            return new Relation(from, to, type, relationLabel, cardinality);
        }
    }

    /**
     * Static factory method to create a Relation from given parameters.
     *
     * You can tailor the signature and logic of this factory method
     * depending on how you derive relationships.
     */
    public static Relation from(UMLClass from, UMLClass to, RelationShipType type, String relationLabel, Cardinality cardinality) {
        return new Builder()
                .from(from)
                .to(to)
                .type(type)
                .relationLabel(relationLabel)
                .cardinality(cardinality)
                .build();
    }

    /**
     * Another example factory method, if you prefer to derive certain parameters from an Element:
     * This is just an example; adjust the logic based on your needs.
     *
     * @param from        the originating UMLClass
     * @param to          the target UMLClass
     * @param element     the Element from which to derive a relationLabel and/or cardinality
     * @param defaultType the default RelationShipType
     */
    public static Relation fromElement(UMLClass from, UMLClass to, Element element, RelationShipType defaultType) {
        return new Builder()
                .from(from)
                .to(to)
                .type(defaultType)
                .relationLabel(element.getName()) // perhaps use element name as a label
                .cardinality(element.getCardinality())
                .build();
    }

    @Override
    public String toString() {
        return String.format("\"%s\" %s \"%s\" \"%s\" : **%s**\n", getFrom().matchTitle(), getType().toArrow(), getCardinality().relationString(), getTo().matchTitle(), getRelationLabel());
    }
}
