package com.fhir.server.util;

public enum ContentDispositionType {
    INLINE("inline"),
    ATTACHMENT("attachment");

    private final String dispositionName;

    ContentDispositionType(String dispositionName) {
        this.dispositionName = dispositionName;
    }

    public String getDispositionName() {
        return dispositionName;
    }

    public static ParsedContentDisposition parse(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return new ParsedContentDisposition(INLINE, null);
        }

        String lowerCase = headerValue.toLowerCase();
        if (lowerCase.startsWith("attachment")) {
            String filename = extractFilename(headerValue);
            return new ParsedContentDisposition(ATTACHMENT, filename);
        } else {
            return new ParsedContentDisposition(INLINE, null);
        }
    }

    private static String extractFilename(String headerValue) {
        int start = headerValue.indexOf("filename=\"");
        if (start == -1) {
            return null; // не нашли
        }
        int end = headerValue.indexOf("\"", start + 10);
        if (end == -1) {
            return null; // не нашли закрывающую кавычку
        }
        return headerValue.substring(start + 10, end);
    }

    public static class ParsedContentDisposition {
        private final ContentDispositionType type;
        private final String filename;

        public ParsedContentDisposition(ContentDispositionType type, String filename) {
            this.type = type;
            this.filename = filename;
        }

        public ContentDispositionType getType() {
            return type;
        }

        public String getFilename() {
            return filename;
        }

        public String toHeaderValue() {
            if (type == ContentDispositionType.ATTACHMENT && filename != null && !filename.isBlank()) {
                return "attachment; filename=\"" + filename + "\"";
            } else if (type == ContentDispositionType.ATTACHMENT) {
                return "attachment";
            } else {
                return "inline";
            }
        }
    }
}
