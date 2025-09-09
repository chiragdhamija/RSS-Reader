package com.sismics.reader.core.dao.file.rss;

public enum FeedFormat {
    RSS,
    ATOM,
    RDF;

    public static FeedFormat fromRootElement(String rootElement) {
        if (rootElement == null) {
            throw new IllegalArgumentException("Root element cannot be null");
        }
        switch (rootElement.toLowerCase()) {
            case "rss": return RSS;
            case "feed": return ATOM;
            case "rdf": return RDF;
            default: throw new IllegalArgumentException("Unknown feed format: " + rootElement);
        }
    }
}