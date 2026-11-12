package com.example.dataset.service;

import com.example.dataset.model.SortOrder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Sorts records by any field. Numbers compare numerically, text case-insensitively, nulls always last. */
@Component
public class RecordSorter {

    public List<Map<String, Object>> sort(List<Map<String, Object>> records, String field, SortOrder order) {
        Comparator<Object> valueComparator = order.isAscending()
                ? FieldValueComparator.INSTANCE
                : FieldValueComparator.INSTANCE.reversed();

        Comparator<Map<String, Object>> comparator = Comparator.comparing(
                (Map<String, Object> record) -> record.get(field),
                Comparator.nullsLast(valueComparator));

        List<Map<String, Object>> sorted = new ArrayList<>(records);
        sorted.sort(comparator);
        return sorted;
    }
}
