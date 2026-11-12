package com.example.dataset.service;

import com.example.dataset.exception.DatasetNotFoundException;
import com.example.dataset.exception.DuplicateRecordException;
import com.example.dataset.exception.InvalidRequestException;
import com.example.dataset.model.DatasetRecord;
import com.example.dataset.model.InsertRecordResponse;
import com.example.dataset.model.SortOrder;
import com.example.dataset.repository.DatasetRecordRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Orchestrates storing and querying dataset records; the heavy lifting lives in small collaborators. */
@Service
public class DatasetService {

    private static final Pattern DATASET_NAME = Pattern.compile("^[A-Za-z0-9_-]{1,100}$");

    private final DatasetRecordRepository repository;
    private final RecordJsonMapper jsonMapper;
    private final RecordSorter sorter;
    private final RecordGrouper grouper;

    public DatasetService(DatasetRecordRepository repository,
                          RecordJsonMapper jsonMapper,
                          RecordSorter sorter,
                          RecordGrouper grouper) {
        this.repository = repository;
        this.jsonMapper = jsonMapper;
        this.sorter = sorter;
        this.grouper = grouper;
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

        DatasetRecord saved = repository.save(new DatasetRecord(dataset, externalId, jsonMapper.toJson(body)));
        return new InsertRecordResponse("Record added successfully", dataset, id != null ? id : saved.getId());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> query(String dataset, String groupBy, String sortBy, String order) {
        validateDatasetName(dataset);
        SortOrder sortOrder = SortOrder.from(order);

        List<Map<String, Object>> records = repository.findByDatasetNameOrderByIdAsc(dataset).stream()
                .map(stored -> jsonMapper.fromJson(stored.getData()))
                .toList();
        if (records.isEmpty()) {
            throw new DatasetNotFoundException(dataset);
        }

        // sort first, then group: grouping keeps order, so every group comes out sorted too
        List<Map<String, Object>> working = hasText(sortBy) ? sorter.sort(records, sortBy, sortOrder) : records;

        Map<String, Object> response = new LinkedHashMap<>();
        if (hasText(groupBy)) {
            response.put("groupedRecords", grouper.group(working, groupBy));
        } else if (hasText(sortBy)) {
            response.put("sortedRecords", working);
        } else {
            response.put("records", working);
        }
        return response;
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
}
