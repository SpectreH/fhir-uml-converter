package org.fhir.uml.generation.uml.elements;

import java.util.Objects;

public class Cardinality {
    private String min;
    private String max;

    public Cardinality(String min, String max) {
        this.min = min;
        this.max = max;
    }

    public Cardinality(String cardinality) {
        if (cardinality != null && cardinality.matches("\\d+\\.\\.\\*?\\d*")) {
            String[] parts = cardinality.split("\\.\\.");
            this.min = parts[0];
            this.max = parts.length > 1 ? parts[1] : "*"; // Defaults to * if not provided
        } else {
            throw new IllegalArgumentException("Invalid cardinality format. Expected format: x..y");
        }
    }

    public String getMin() {
        return min;
    }

    public String getMax() {
        return max;
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
