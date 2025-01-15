package com.example.fhir_uml_converter.util;

import org.springframework.http.MediaType;

import java.util.List;

public enum BodyMediaType {
    FHIR_JSON("application", "fhir+json"),
    JSON("application", "json"),
    FHIR_XML("application", "fhir+xml");

    private final String type;
    private final String subtype;

    BodyMediaType(String type, String subtype) {
        this.type = type;
        this.subtype = subtype;
    }

    public String getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    public boolean matches(MediaType mediaType) {
        return mediaType.getType().equalsIgnoreCase(this.type)
                && mediaType.getSubtype().equalsIgnoreCase(this.subtype);
    }

    public static BodyMediaType fromMediaTypes(List<MediaType> mediaTypes) {
        if (mediaTypes == null || mediaTypes.isEmpty()) {
            return FHIR_JSON;
        }

        for (MediaType candidate : mediaTypes) {
            for (BodyMediaType mt : BodyMediaType.values()) {
                if (mt.matches(candidate)) {
                    return mt;
                }
            }
        }

        return FHIR_JSON;
    }
}
