package com.example.dataset.service;

import java.util.Comparator;

/**
 * Compares JSON field values: numbers numerically, everything else as case-insensitive text.
 * Null handling is done by the caller (nulls always sort last).
 */
final class FieldValueComparator implements Comparator<Object> {

    static final FieldValueComparator INSTANCE = new FieldValueComparator();

    private FieldValueComparator() {
    }

    @Override
    public int compare(Object a, Object b) {
        if (a instanceof Number x && b instanceof Number y) {
            return Double.compare(x.doubleValue(), y.doubleValue());
        }
        return a.toString().compareToIgnoreCase(b.toString());
    }
}
