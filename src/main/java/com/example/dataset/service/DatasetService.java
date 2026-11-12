package com.example.dataset.service;

import com.example.dataset.exception.DatasetNotFoundException;
import com.example.dataset.exception.DuplicateRecordException;
import com.example.dataset.exception.InvalidRequestException;
import com.example.dataset.model.DatasetRecord;
import com.example.dataset.model.InsertRecordResponse;
import com.example.dataset.repository.DatasetRecordRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatasetService {

    private static final Pattern DATASET_NAME = Pattern.compile("^[A-Za-z0-9_-]{1,100}$");
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() { };

    private final DatasetRecordRepository repository;
    private final ObjectMapper objectMapper;

    public DatasetService(DatasetRecordRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public InsertRecordResponse addRecord(String dataset, Map<String, Object> body) {
        validateDatasetName(dataset);
        if (body == null || body.isEmpty()) {
            throw new InvalidRequestException("Record body must be a non-empty JSON object");
        }

        Object id = body.get("id");
        String externalId = id == null ? null : String.valueOf(id);
        if (externalId != null && repository.existsByDatasetNameAndRecordId(dataset, externalId)) {
            throw new DuplicateRecordException(dataset, externalId);
        }

        DatasetRecord saved = repository.save(new DatasetRecord(dataset, externalId, toJson(body)));
        return new InsertRecordResponse("Record added successfully", dataset, id != null ? id : saved.getId());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> query(String dataset, String groupBy, String sortBy, String order) {
        validateDatasetName(dataset);
        boolean ascending = parseOrder(order);

        List<Map<String, Object>> records = repository.findByDatasetNameOrderByIdAsc(dataset).stream()
                .map(r -> fromJson(r.getData()))
                .toList();
        if (records.isEmpty()) {
            throw new DatasetNotFoundException(dataset);
        }

        List<Map<String, Object>> working = new ArrayList<>(records);
        if (hasText(sortBy)) {
            working.sort(buildComparator(sortBy, ascending));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        if (hasText(groupBy)) {
            // sorting happened first and grouping keeps insertion order, so each group is sorted too
            response.put("groupedRecords", groupRecords(working, groupBy));
        } else if (hasText(sortBy)) {
            response.put("sortedRecords", working);
        } else {
            response.put("records", working);
        }
        return response;
    }

    private Map<String, List<Map<String, Object>>> groupRecords(List<Map<String, Object>> records, String field) {
        Map<String, List<Map<String, Object>>> groups = new LinkedHashMap<>();
        for (Map<String, Object> record : records) {
            String key = String.valueOf(record.get(field)); // records missing the field land under "null"
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(record);
        }
        return groups;
    }

    private Comparator<Map<String, Object>> buildComparator(String field, boolean ascending) {
        Comparator<Object> valueComparator = ascending
                ? FieldValueComparator.INSTANCE
                : FieldValueComparator.INSTANCE.reversed();
        // nulls always sort last, whichever direction is requested
        return Comparator.comparing((Map<String, Object> r) -> r.get(field), Comparator.nullsLast(valueComparator));
    }

    private boolean parseOrder(String order) {
        if ("asc".equalsIgnoreCase(order)) {
            return true;
        }
        if ("desc".equalsIgnoreCase(order)) {
            return false;
        }
        throw new InvalidRequestException("Query parameter 'order' must be 'asc' or 'desc'");
    }

    private void validateDatasetName(String dataset) {
        if (dataset == null || !DATASET_NAME.matcher(dataset).matches()) {
            throw new InvalidRequestException(
                    "Dataset name must be 1-100 characters: letters, digits, '_' or '-'");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String toJson(Map<String, Object> body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new InvalidRequestException("Record could not be serialised to JSON");
        }
    }

    private Map<String, Object> fromJson(String json) {
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Stored record is corrupt", e);
        }
    }
}
