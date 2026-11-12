package com.example.dataset.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RecordGrouperTest {

    private final RecordGrouper grouper = new RecordGrouper();

    private static Map<String, Object> rec(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    @Test
    void group_groupsByFieldValueAndKeepsOrder() {
        List<Map<String, Object>> input = List.of(
                rec("id", 1, "department", "Engineering"),
                rec("id", 2, "department", "Marketing"),
                rec("id", 3, "department", "Engineering"));

        Map<String, List<Map<String, Object>>> result = grouper.group(input, "department");

        assertEquals(List.of("Engineering", "Marketing"), List.copyOf(result.keySet()));
        assertEquals(List.of(1, 3), result.get("Engineering").stream().map(r -> r.get("id")).toList());
        assertEquals(1, result.get("Marketing").size());
    }

    @Test
    void group_putsRecordsWithoutTheFieldUnderNull() {
        List<Map<String, Object>> input = List.of(rec("id", 1, "department", "Engineering"), rec("id", 2));

        Map<String, List<Map<String, Object>>> result = grouper.group(input, "department");

        assertEquals(1, result.get("null").size());
    }

    @Test
    void group_numericValuesBecomeTextKeys() {
        List<Map<String, Object>> input = List.of(rec("id", 1, "age", 30), rec("id", 2, "age", 30));

        Map<String, List<Map<String, Object>>> result = grouper.group(input, "age");

        assertEquals(2, result.get("30").size());
    }
}
