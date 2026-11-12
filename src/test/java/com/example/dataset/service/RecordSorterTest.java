package com.example.dataset.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.dataset.model.SortOrder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RecordSorterTest {

    private final RecordSorter sorter = new RecordSorter();

    private static Map<String, Object> rec(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    @Test
    void sort_ascendingByNumber() {
        List<Map<String, Object>> input = List.of(rec("id", 1, "age", 30), rec("id", 2, "age", 25), rec("id", 3, "age", 28));

        List<Map<String, Object>> result = sorter.sort(input, "age", SortOrder.ASC);

        assertEquals(List.of(2, 3, 1), result.stream().map(r -> r.get("id")).toList());
    }

    @Test
    void sort_descendingByNumber() {
        List<Map<String, Object>> input = List.of(rec("id", 1, "age", 30), rec("id", 2, "age", 25), rec("id", 3, "age", 28));

        List<Map<String, Object>> result = sorter.sort(input, "age", SortOrder.DESC);

        assertEquals(List.of(1, 3, 2), result.stream().map(r -> r.get("id")).toList());
    }

    @Test
    void sort_comparesNumbersNumericallyNotAsText() {
        List<Map<String, Object>> input = List.of(rec("id", 1, "score", 10), rec("id", 2, "score", 9));

        List<Map<String, Object>> result = sorter.sort(input, "score", SortOrder.ASC);

        assertEquals(List.of(2, 1), result.stream().map(r -> r.get("id")).toList());
    }

    @Test
    void sort_comparesTextIgnoringCase() {
        List<Map<String, Object>> input = List.of(rec("id", 1, "name", "bob"), rec("id", 2, "name", "Alice"));

        List<Map<String, Object>> result = sorter.sort(input, "name", SortOrder.ASC);

        assertEquals(List.of(2, 1), result.stream().map(r -> r.get("id")).toList());
    }

    @Test
    void sort_putsMissingValuesLastInBothDirections() {
        List<Map<String, Object>> input = List.of(rec("id", 1), rec("id", 2, "age", 40), rec("id", 3, "age", 20));

        assertEquals(List.of(3, 2, 1),
                sorter.sort(input, "age", SortOrder.ASC).stream().map(r -> r.get("id")).toList());
        assertEquals(List.of(2, 3, 1),
                sorter.sort(input, "age", SortOrder.DESC).stream().map(r -> r.get("id")).toList());
    }

    @Test
    void sort_doesNotModifyTheInputList() {
        List<Map<String, Object>> input = List.of(rec("id", 1, "age", 30), rec("id", 2, "age", 25));

        sorter.sort(input, "age", SortOrder.ASC);

        assertEquals(1, input.get(0).get("id"));
    }
}
