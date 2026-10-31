package com.tbscg.doosan.magnolia.cloudinary.api;

import java.util.Iterator;

public class SearchResult {
    private final Iterator<ApiObject> results;
    private final int totalCount;

    public SearchResult(Iterator<ApiObject> results, int totalCount) {
        this.results = results;
        this.totalCount = totalCount;
    }

    public Iterator<ApiObject> getResults() {
        return results;
    }

    public int getTotalCount() {
        return totalCount;
    }
}
