package com.example.dataset.model;

import com.example.dataset.exception.InvalidRequestException;

/** Sort direction accepted by the query API. */
public enum SortOrder {
    ASC,
    DESC;

    /** Parses the {@code order} query parameter. A missing value means ascending. */
    public static SortOrder from(String value) {
        if (value == null) {
            return ASC;
        }
        return switch (value.trim().toLowerCase()) {
            case "asc" -> ASC;
            case "desc" -> DESC;
            default -> throw new InvalidRequestException("Query parameter 'order' must be 'asc' or 'desc'");
        };
    }

    public boolean isAscending() {
        return this == ASC;
    }
}
