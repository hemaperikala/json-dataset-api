package com.example.dataset.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Groups records by the value of a field, keeping the incoming record order inside each group. */
@Component
public class RecordGrouper {

    public Map<String, List<Map<String, Object>>> group(List<Map<String, Object>> records, String field) {
        Map<String, List<Map<String, Object>>> groups = new LinkedHashMap<>();
        for (Map<String, Object> record : records) {
            String key = String.valueOf(record.get(field)); // records missing the field land under "null"
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(record);
        }
        return groups;
    }
}
