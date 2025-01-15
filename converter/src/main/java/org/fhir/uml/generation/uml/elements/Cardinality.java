package org.fhir.uml.generation.uml.elements;

import java.util.Objects;

public class Cardinality {
    private String min;
    private String max;

    public Cardinality(String min, String max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public String toString() {
        return String.format("[%s..%s]", min, max);
    }

    public String relationString() {
        return String.format("%s..%s", min, max);
    }

    public Boolean isRemoved() {
        return Objects.equals(min, "0") && Objects.equals(max, "0");
    }
}
