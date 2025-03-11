package org.fhir.uml.generation.uml.elements;

public class Constraint {
    private String key;
    private String severity;
    private String human;

    public Constraint(String key, String severity, String human) {
        this.key = key;
        this.severity = severity;
        this.human = human;
    }

    public String getKey() {
        return key;
    }

    public String getSeverity() {
        return severity;
    }

    public String getHuman() {
        return human;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public void setHuman(String human) {
        this.human = human;
    }
}
