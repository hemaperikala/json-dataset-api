package com.example.dataset.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.dataset.exception.DatasetNotFoundException;
import com.example.dataset.exception.DuplicateRecordException;
import com.example.dataset.exception.InvalidRequestException;
import com.example.dataset.model.DatasetRecord;
import com.example.dataset.model.InsertRecordResponse;
import com.example.dataset.repository.DatasetRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatasetServiceTest {

    @Mock
    private DatasetRecordRepository repository;

    private DatasetService service;

    @BeforeEach
    void setUp() {
        service = new DatasetService(repository, new RecordJsonMapper(new ObjectMapper()),
                new RecordSorter(), new RecordGrouper());
    }

    private static DatasetRecord stored(int id, String name, int age, String department) {
        String json = String.format("{\"id\":%d,\"name\":\"%s\",\"age\":%d,\"department\":\"%s\"}",
                id, name, age, department);
        return new DatasetRecord("emp", String.valueOf(id), json);
    }

    private List<DatasetRecord> employees() {
        return List.of(stored(1, "John Doe", 30, "Engineering"),
                stored(2, "Jane Smith", 25, "Engineering"),
                stored(3, "Alice Brown", 28, "Marketing"));
    }

    // ---------- addRecord ----------

    @Test
    void addRecord_savesRecordAndReturnsItsId() {
        when(repository.save(any(DatasetRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InsertRecordResponse response = service.addRecord("emp", Map.of("id", 1, "name", "John Doe"));

        assertEquals("Record added successfully", response.message());
        assertEquals("emp", response.dataset());
        assertEquals(1, response.recordId());
        verify(repository).save(any(DatasetRecord.class));
    }

    @Test
    void addRecord_duplicateId_isRejectedAndNothingIsSaved() {
        when(repository.existsByDatasetNameAndRecordId("emp", "1")).thenReturn(true);

        assertThrows(DuplicateRecordException.class, () -> service.addRecord("emp", Map.of("id", 1)));

        verify(repository, never()).save(any(DatasetRecord.class));
    }

    @Test
    void addRecord_emptyBody_isRejected() {
        assertThrows(InvalidRequestException.class, () -> service.addRecord("emp", Map.of()));
    }

    @Test
    void addRecord_invalidDatasetName_isRejected() {
        assertThrows(InvalidRequestException.class, () -> service.addRecord("bad name!", Map.of("id", 1)));
    }

    // ---------- query ----------

    @Test
    @SuppressWarnings("unchecked")
    void query_groupBy_returnsGroupedRecords() {
        when(repository.findByDatasetNameOrderByIdAsc("emp")).thenReturn(employees());

        Map<String, Object> result = service.query("emp", "department", null, "asc");

        Map<String, List<Map<String, Object>>> grouped =
                (Map<String, List<Map<String, Object>>>) result.get("groupedRecords");
        assertEquals(2, grouped.get("Engineering").size());
        assertEquals(1, grouped.get("Marketing").size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void query_sortByAsc_returnsSortedRecords() {
        when(repository.findByDatasetNameOrderByIdAsc("emp")).thenReturn(employees());

        Map<String, Object> result = service.query("emp", null, "age", "asc");

        List<Map<String, Object>> sorted = (List<Map<String, Object>>) result.get("sortedRecords");
        assertEquals(List.of("Jane Smith", "Alice Brown", "John Doe"),
                sorted.stream().map(r -> r.get("name")).toList());
    }

    @Test
    @SuppressWarnings("unchecked")
    void query_sortByDesc_reversesTheOrder() {
        when(repository.findByDatasetNameOrderByIdAsc("emp")).thenReturn(employees());

        Map<String, Object> result = service.query("emp", null, "age", "desc");

        List<Map<String, Object>> sorted = (List<Map<String, Object>>) result.get("sortedRecords");
        assertEquals("John Doe", sorted.get(0).get("name"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void query_groupByAndSortBy_sortsInsideEachGroup() {
        when(repository.findByDatasetNameOrderByIdAsc("emp")).thenReturn(employees());

        Map<String, Object> result = service.query("emp", "department", "age", "asc");

        Map<String, List<Map<String, Object>>> grouped =
                (Map<String, List<Map<String, Object>>>) result.get("groupedRecords");
        assertEquals("Jane Smith", grouped.get("Engineering").get(0).get("name"));
    }

    @Test
    void query_withoutParameters_returnsAllRecords() {
        when(repository.findByDatasetNameOrderByIdAsc("emp")).thenReturn(employees());

        Map<String, Object> result = service.query("emp", null, null, "asc");

        assertTrue(result.containsKey("records"));
    }

    @Test
    void query_unknownDataset_throwsNotFound() {
        when(repository.findByDatasetNameOrderByIdAsc("ghost")).thenReturn(List.of());

        assertThrows(DatasetNotFoundException.class, () -> service.query("ghost", "department", null, "asc"));
    }

    @Test
    void query_invalidOrder_isRejectedBeforeTouchingTheDatabase() {
        assertThrows(InvalidRequestException.class, () -> service.query("emp", null, "age", "sideways"));

        verify(repository, never()).findByDatasetNameOrderByIdAsc(any());
    }
}
