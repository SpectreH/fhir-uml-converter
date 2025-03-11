package org.fhir.uml.generation.uml.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.fhir.uml.generation.uml.types.LegendPosition.XPosition;
import org.fhir.uml.generation.uml.types.LegendPosition.YPosition;

/**
 * Extended Legend class that supports multiple groups, each with
 * its own title, header columns, and rows of data.
 */
public class Legend {

    private XPosition xPosition;
    private YPosition yPosition;

    // A list to hold multiple groups
    private final List<LegendGroup> groups = new ArrayList<>();

    /**
     * Adds a new group (with a title) and returns it,
     * so you can chain group-specific configurations.
     */
    public LegendGroup addGroup(String title) {
        LegendGroup group = new LegendGroup(title);
        groups.add(group);
        return group;
    }

    /**
     * Returns all groups (read-only).
     */
    public List<LegendGroup> getGroups() {
        return List.copyOf(groups);
    }

    @Override
    public String toString() {
        // If no groups, return empty string
        if (groups.isEmpty()) {
            return "";
        }

        // Default positions if not set
        XPosition xPos = (xPosition != null) ? xPosition : XPosition.RIGHT;
        YPosition yPos = (yPosition != null) ? yPosition : YPosition.TOP;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("legend %s %s%n",
                yPos.name().toLowerCase(),
                xPos.name().toLowerCase()));

        // For each group, print the group’s title, header, rows
        for (LegendGroup group : groups) {
            // Print group title in PlantUML “= Title” style
            if (group.getTitle() != null && !group.getTitle().isBlank()) {
                sb.append(String.format("= %s%n%n", group.getTitle()));
            }

            // If there is a header, print it in “|= col1 |= col2 |= col3 |” style
            List<String> header = group.getHeader();
            if (header != null && !header.isEmpty()) {
                sb.append("|= ");
                sb.append(String.join(" |= ", header));
                sb.append(" |").append(System.lineSeparator());
            }

            // Print each row
            for (List<String> row : group.getRows()) {
                sb.append("| ");
                sb.append(String.join(" | ", row));
                sb.append(" |").append(System.lineSeparator());
            }

            sb.append(System.lineSeparator());
        }

        sb.append("end legend\n");
        return sb.toString();
    }

    // -- Getters/setters for xPosition and yPosition
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

    // -------------------------------------
    // Inner class LegendGroup
    // -------------------------------------
    public static class LegendGroup {

        private final String title;
        private List<String> header = new ArrayList<>();
        private final List<List<String>> rows = new ArrayList<>();

        public LegendGroup(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public List<String> getHeader() {
            return header;
        }

        public List<List<String>> getRows() {
            return rows;
        }

        /**
         * Sets the column headers for the group.
         * For instance: setHeader("Type", "Value")
         */
        public LegendGroup setHeader(String... columns) {
            this.header = Arrays.asList(columns);
            return this;
        }

        /**
         * Adds a row to this group’s table.
         * For instance: addRow("url", "https://fhir.ee/base/StructureDefinition/...")
         */
        public LegendGroup addRow(String... columns) {
            this.rows.add(Arrays.asList(columns));
            return this;
        }
    }
}
