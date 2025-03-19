package org.fhir.uml.generation.uml.types;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum CustomClassType {
    SLICES("S", "#FF7700", "Slices");

    private final String letter;
    private final String color;
    private final String label;

    // Regex to parse something like: << (S,#FF7700) Slices >>
    private static final Pattern PARSE_PATTERN = Pattern.compile(
            "^<<\\s*\\(([^,]+),([^)]+)\\)\\s*(.*?)\\s*>>$"
    );

    CustomClassType(String letter, String color, String label) {
        this.letter = letter;
        this.color = color;
        this.label = label;
    }

    public String getLetter() {
        return letter;
    }

    public String getColor() {
        return color;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Returns the UML syntax, for example:
     *  << (S,#FF7700) Slices >>
     */
    @Override
    public String toString() {
        return String.format("<< (%s,%s) %s >>", letter, color, label);
    }

    /**
     * Attempts to parse a custom UML class string (group 3 in your regex),
     * for example: "<< (S,#FF7700) Slices >>"
     * and match it to one of the known enum constants.
     *
     * @param text the substring captured from the regex
     * @return the corresponding CustomClassType if it matches, or null if not found
     */
    public static CustomClassType fromUmlString(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        Matcher matcher = PARSE_PATTERN.matcher(text.trim());
        if (matcher.matches()) {
            String letterValue = matcher.group(1);
            String colorValue  = matcher.group(2);
            String labelValue  = matcher.group(3);
            for (CustomClassType type : values()) {
                if (type.letter.equals(letterValue) &&
                        type.color.equals(colorValue)  &&
                        type.label.equals(labelValue))
                {
                    return type;
                }
            }
        }
        return null;
    }
}
