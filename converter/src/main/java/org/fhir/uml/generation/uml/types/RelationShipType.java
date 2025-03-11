package org.fhir.uml.generation.uml.types;

public enum RelationShipType {
        ASSOCIATION, DIRECTED_ASSOCIATION, AGGREGATION, COMPOSITION, DEPENDENCY, MULTIPLICITY, INHERITANCE, IMPLEMENTATION;

        public static RelationShipType fromArrow(String arrow) {
                return switch (arrow) {
                        case "--" -> ASSOCIATION;
                        case "-->" -> DIRECTED_ASSOCIATION;
                        case "o--" -> AGGREGATION;
                        case "*--" -> COMPOSITION;
                        case "..>" -> DEPENDENCY;
                        case "1..*" -> MULTIPLICITY;
                        case "|--" -> INHERITANCE;
                        case "|>" -> IMPLEMENTATION;
                        default -> throw new IllegalArgumentException("Unknown UML arrow: " + arrow);
                };
        }

        public String toArrow() {
                return switch (this) {
                        case ASSOCIATION -> "--";
                        case DIRECTED_ASSOCIATION -> "-->";
                        case AGGREGATION -> "o--";
                        case COMPOSITION -> "*--";
                        case DEPENDENCY -> "..>";
                        case MULTIPLICITY -> "1..*";
                        case INHERITANCE -> "|--";
                        case IMPLEMENTATION -> "|>";
                };
        }
}
