package com.example.dataset.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.dataset.exception.InvalidRequestException;
import com.example.dataset.model.SortOrder;
import org.junit.jupiter.api.Test;

class SortOrderTest {

    @Test
    void from_parsesAscAndDescIgnoringCase() {
        assertEquals(SortOrder.ASC, SortOrder.from("asc"));
        assertEquals(SortOrder.DESC, SortOrder.from("DESC"));
        assertEquals(SortOrder.DESC, SortOrder.from(" desc "));
    }

    @Test
    void from_nullDefaultsToAscending() {
        assertEquals(SortOrder.ASC, SortOrder.from(null));
    }

    @Test
    void from_rejectsUnknownValue() {
        assertThrows(InvalidRequestException.class, () -> SortOrder.from("sideways"));
    }
}
