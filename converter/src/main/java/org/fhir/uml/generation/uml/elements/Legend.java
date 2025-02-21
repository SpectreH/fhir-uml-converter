package org.fhir.uml.generation.uml.elements;

import java.util.LinkedHashMap;
import java.util.Map;
import org.fhir.uml.generation.uml.types.LegendPosition.XPosition;
import org.fhir.uml.generation.uml.types.LegendPosition.YPosition;

/**
 * A simplified Legend class:
 * Stores data as "key -> value", then prints them line-by-line
 * in a PlantUML legend block (no comment ID logic).
 */
public class Legend {

    private XPosition xPosition;
    private YPosition yPosition;

    // Use LinkedHashMap if you want to preserve insertion order
    private final Map<String, String> data = new LinkedHashMap<>();

    /**
     * Adds or updates a key-value entry to the legend.
     */
    public void put(String key, String value) {
        if (key != null && value != null) {
            data.put(key, value);
        }
    }

    /**
     * Returns the value associated with a given key, or null if not present.
     */
    public String get(String key) {
        return data.get(key);
    }

    /**
     * Returns a read-only view of all current key-value pairs.
     */
    public Map<String, String> getData() {
        return Map.copyOf(data);
    }

    @Override
    public String toString() {
        // If there's no data, just return empty string
        if (data.isEmpty()) {
            return "";
        }

        // Default positions if not set
        XPosition xPos = (xPosition != null) ? xPosition : XPosition.RIGHT;
        YPosition yPos = (yPosition != null) ? yPosition : YPosition.TOP;

        // Start building the legend output
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("legend %s %s%n",
                yPos.name().toLowerCase(), xPos.name().toLowerCase()));

        // Print each key: value on its own line
        for (Map.Entry<String, String> entry : data.entrySet()) {
            sb.append(String.format("**%s**: %s%n", entry.getKey(), entry.getValue()));
        }

        sb.append("end legend\n");
        return sb.toString();
    }

    // getters/setters for xPosition and yPosition
    public XPosition getXPosition() {
        return xPosition;
    }

    public void setXPosition(XPosition xPosition) {
        this.xPosition = xPosition;
    }

    public YPosition getYPosition() {
        return yPosition;
    }

    public void setYPosition(YPosition yPosition) {
        this.yPosition = yPosition;
    }
}
