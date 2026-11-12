package com.example.dataset.controller;

import com.example.dataset.exception.ErrorResponse;
import com.example.dataset.model.InsertRecordResponse;
import com.example.dataset.service.DatasetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
            description = "Any JSON object is accepted. If it has an 'id' field, ids must be unique within the dataset.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "Employee",
                                    value = "{\"id\":1,\"name\":\"John Doe\",\"age\":30,\"department\":\"Engineering\"}"))))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Record added",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = InsertRecordResponse.class))),
            @ApiResponse(responseCode = "400", description = "Malformed JSON, empty body or invalid dataset name",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "A record with the same id already exists in the dataset",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/record")
    public ResponseEntity<InsertRecordResponse> insertRecord(
            @Parameter(description = "Dataset name (letters, digits, _ or -)", example = "employee_dataset")
            @PathVariable String datasetName,
            @RequestBody Map<String, Object> record) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addRecord(datasetName, record));
    }

    @Operation(summary = "Query a dataset with optional group-by and sort-by",
            description = "groupBy only -> groupedRecords. sortBy only -> sortedRecords. "
                    + "Both -> groupedRecords with each group sorted. Neither -> records.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Query result"),
            @ApiResponse(responseCode = "400", description = "Invalid dataset name or order",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Dataset does not exist or has no records",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/query")
    public Map<String, Object> query(
            @Parameter(description = "Dataset name", example = "employee_dataset")
            @PathVariable String datasetName,
            @Parameter(description = "Field to group by", example = "department")
            @RequestParam(required = false) String groupBy,
            @Parameter(description = "Field to sort by", example = "age")
            @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction: asc or desc", example = "asc")
            @RequestParam(defaultValue = "asc") String order) {
        return service.query(datasetName, groupBy, sortBy, order);
    }
}
