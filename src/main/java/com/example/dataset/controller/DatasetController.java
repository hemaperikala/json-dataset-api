package com.example.dataset.controller;

import com.example.dataset.model.InsertRecordResponse;
import com.example.dataset.service.DatasetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dataset/{datasetName}")
@Tag(name = "Dataset", description = "Insert and query JSON records")
public class DatasetController {

    private final DatasetService service;

    public DatasetController(DatasetService service) {
        this.service = service;
    }

    @Operation(summary = "Insert a JSON record into a dataset",
            description = "Any JSON object is accepted. If it has an 'id' field, ids must be unique within the dataset.")
    @PostMapping("/record")
    public ResponseEntity<InsertRecordResponse> insertRecord(
            @PathVariable String datasetName,
            @RequestBody Map<String, Object> record) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addRecord(datasetName, record));
    }

    @Operation(summary = "Query a dataset with optional group-by and sort-by",
            description = "groupBy only -> groupedRecords. sortBy only -> sortedRecords. "
                    + "Both -> groupedRecords with each group sorted. Neither -> records.")
    @GetMapping("/query")
    public Map<String, Object> query(
            @PathVariable String datasetName,
            @Parameter(description = "Field to group by, e.g. department") @RequestParam(required = false) String groupBy,
            @Parameter(description = "Field to sort by, e.g. age") @RequestParam(required = false) String sortBy,
            @Parameter(description = "asc or desc") @RequestParam(defaultValue = "asc") String order) {
        return service.query(datasetName, groupBy, sortBy, order);
    }
}
