package org.fhir.uml.generation.uml.elements;

public class Binding {
    private String valueSet;
    private String strength;

    public Binding(String valueSet, String strength) {
        this.valueSet = valueSet;
        this.strength = strength;
    }

    public String getValueSet() {
        return valueSet;
    }

    public void setValueSet(String valueSet) {
        this.valueSet = valueSet;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    @Override
    public String toString() {
        return String.format("{field}<size:10><<<b>Binding</b>: %s (%s)>></size>", Element.getURLLastPath(valueSet), strength);
    }
}
