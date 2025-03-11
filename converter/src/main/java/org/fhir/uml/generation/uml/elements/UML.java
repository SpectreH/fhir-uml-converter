package org.fhir.uml.generation.uml.elements;

import org.fhir.uml.generation.uml.utils.Config;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UML {
    private final LinkedList<UMLClass> classes;
    private final LinkedList<Relation> relations;
    private UMLClass mainClass;
    private Legend legend;
    private final Config config = Config.getInstance();
    private Map<String, Constraint> constraints = new LinkedHashMap<>();

    public UML() {
        this.classes = new LinkedList<>();
        this.relations = new LinkedList<>();
    }

    public void addClass(UMLClass umlClass) {
        if (umlClass != null) {
            this.classes.add(umlClass);

            if (umlClass.isMainClass()) {
                mainClass = umlClass;
            }
        }
    }

    public void addRelation(Relation relation) {
        if (relation != null) {
            this.relations.add(relation);
        }
    }

    public UMLClass findClassByTitle(String title) {
        for (UMLClass umlClass : classes) {
            if (umlClass.getTitle().equals(title)) {
                return umlClass;
            }
        }
        return null;
    }

    public UMLClass findClassByElement(Element element) {
        for (UMLClass umlClass : classes) {
            if (umlClass.getMainElement().equals(element)) {
                return umlClass;
            }
        }
        return null;
    }

    public UMLClass getMainClass() {
        return mainClass;
    }

    public void setMainClass(UMLClass mainClass) {
        this.mainClass = mainClass;
    }

    public List<UMLClass> getClasses() {
        return this.classes;
    }

    public List<Relation> getRelations() {
        return this.relations;
    }

    public void setLegend(Legend legend) {
        this.legend = legend;
    }

    public Map<String, Constraint> getConstraints() {
        return constraints;
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("@startuml\n");
        sb.append("hide empty members\n");
        sb.append("skinparam wrapwidth 500\n");
        sb.append("left to right direction\n");

        sb.append("skinparam classStereotypeFontColor black\n");

        if (config.isDifferential()) {
            sb.append("skinparam classAttributeFontColor #808080\n");
        }

        sb.append("skinparam legendFontSize 10\n");
        sb.append("skinparam legendFontColor #333333\n");
        sb.append("skinparam legendBackgroundColor #F2F2F2\n");
        sb.append("skinparam legendBorderColor #999999\n");

        sb.append("!function bold($value)\n");
        sb.append("!return \"<b>\" + $value + \"</b>\"\n");
        sb.append("!endfunction\n");

        sb.append("!function black($value)\n");
        sb.append("!return \"<color:Black>\" + $value + \"</color>\"\n");
        sb.append("!endfunction\n");

        sb.append("!function strikethrough($value)\n");
        sb.append("!return \"<s>\" + $value + \"</s>\"\n");
        sb.append("!endfunction\n");

        sb.append("!function wrap2($text, $width=40)\n");
        sb.append("!if %strlen($text) <= $width\n");
        sb.append("!return $text\n");
        sb.append("!else\n");
        sb.append("!$out = \"\"\n");
        sb.append("!$pos = $width - 1\n");
        sb.append("!$cut = \"\"\n");
        sb.append("!while %strlen($text) > $width\n");
        sb.append("!while $pos >= 0 && $cut == \"\"\n");
        sb.append("!if %substr($text, $pos, 1) == \" \"\n");
        sb.append("!$cut = $pos\n");
        sb.append("!else\n");
        sb.append("!$pos = $pos - 1\n");
        sb.append("!endif\n");
        sb.append("!endwhile\n");
        sb.append("!if $pos < 0\n");
        sb.append("!$out = $out + %substr($text, 0, $width - 1) + \"<U+2936><U+2AFD>\"\n");
        sb.append("!$text = %substr($text, $width - 1, %strlen($text) - $width + 1)\n");
        sb.append("!else\n");
        sb.append("!$out = $out + %substr($text, 0, $cut)\n");
        sb.append("!$text = %substr($text, $cut + 1, %strlen($text) - $cut)\n");
        sb.append("!endif\n");
        sb.append("!if %strlen($text) > 0\n");
        sb.append("!$out = $out + \"\\n\"\n");
        sb.append("!endif\n");
        sb.append("!$pos = $width - 1\n");
        sb.append("!$cut = \"\"\n");
        sb.append("!endwhile\n");
        sb.append("!if %strlen($text) > 0\n");
        sb.append("!$out = $out + $text\n");
        sb.append("!endif\n");
        sb.append("!endif\n");
        sb.append("!return $out\n");
        sb.append("!endfunction\n");

        sb.append("\n");

        for (UMLClass umlClass : this.classes) {
            if (config.isHideRemovedObjects() && umlClass.isParentElementIsRemoved()) {
                continue;
            }
            sb.append(umlClass);
        }

        for (Relation relation : this.relations) {
            if (config.isHideRemovedObjects() & relation.getCardinality().isRemoved()) {
                continue;
            }
            sb.append(relation);
        }

        if (this.legend != null) {
            sb.append(this.legend);
        }

        sb.append("@enduml");
        return sb.toString();
    }
}
