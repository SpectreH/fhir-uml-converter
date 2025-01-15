package com.fhir.server.util;

import org.springframework.http.MediaType;

import java.util.List;

public enum ViewMode {
    SNAPSHOT("snapshot"),
    DIFFERENTIAL("differential");

    private final String viewValue;

    ViewMode(String viewValue) {
        this.viewValue = viewValue;
    }

    public String getViewValue() {
        return viewValue;
    }

    public static ViewMode fromString(String value) {
        if (value == null) {
            return SNAPSHOT;
        }
        for (ViewMode vm : ViewMode.values()) {
            if (vm.viewValue.equalsIgnoreCase(value)) {
                return vm;
            }
        }
        return SNAPSHOT;
    }

    public static ViewMode fromMediaTypes(List<MediaType> mediaTypes) {
        if (mediaTypes == null || mediaTypes.isEmpty()) {
            return SNAPSHOT;
        }
        MediaType first = mediaTypes.get(0);
        String viewParam = first.getParameter("view");
        return fromString(viewParam);
    }
}
