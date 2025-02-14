package org.fhir.uml.generation.uml.types;

public enum ElementVisability {
    PRIVATE, PROTECTED, PUBLIC, PACKAGE_PRIVATE;

    public static ElementVisability fromSymbol(String symbol) {
        return switch (symbol) {
            case "-" -> PRIVATE;
            case "#" -> PROTECTED;
            case "~" -> PACKAGE_PRIVATE;
            default -> PUBLIC;
        };
    }

    public String toSymbol() {
        return switch (this) {
            case PRIVATE -> "-";
            case PROTECTED -> "#";
            case PACKAGE_PRIVATE -> "~";
            case PUBLIC -> "+";
        };
    }
}
