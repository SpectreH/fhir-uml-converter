package com.fhir.server.util;

public class Config {
    String imageType;
    String contentType;
    String view;
    String mode;
    String attachmentFileName;
    Boolean hideRemovedObjects;
    Boolean showConstraints;
    Boolean showBindings;
    Boolean reduceSliceClasses;
    Boolean hideLegend;

    public Config(String imageType, String contentType, String view, String mode, String attachmentFileName, Boolean hideRemovedObjects, Boolean showConstraints, Boolean showBindings, Boolean reduceSliceClasses, Boolean hideLegend) {
        this.imageType = imageType;
        this.contentType = contentType;
        this.view = view;
        this.mode = mode;
        this.attachmentFileName = attachmentFileName;
        this.hideRemovedObjects = hideRemovedObjects;
        this.showConstraints = showConstraints;
        this.showBindings = showBindings;
        this.reduceSliceClasses = reduceSliceClasses;
        this.hideLegend = hideLegend;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getAttachmentFileName() {
        return attachmentFileName;
    }

    public void setAttachmentFileName(String attachmentFileName) {
        this.attachmentFileName = attachmentFileName;
    }

    public Boolean getHideRemovedObjects() {
        return hideRemovedObjects;
    }

    public void setHideRemovedObjects(Boolean hideRemovedObjects) {
        this.hideRemovedObjects = hideRemovedObjects;
    }

    public Boolean getShowConstraints() {
        return showConstraints;
    }

    public void setShowConstraints(Boolean showConstraints) {
        this.showConstraints = showConstraints;
    }

    public Boolean getShowBindings() {
        return showBindings;
    }

    public void setShowBindings(Boolean showBindings) {
        this.showBindings = showBindings;
    }

    public Boolean getReduceSliceClasses() {
        return reduceSliceClasses;
    }

    public void setReduceSliceClasses(Boolean reduceSliceClasses) {
        this.reduceSliceClasses = reduceSliceClasses;
    }

    public Boolean getHideLegend() {
        return hideLegend;
    }

    public void setHideLegend(Boolean hideLegend) {
        this.hideLegend = hideLegend;
    }
}

