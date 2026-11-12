package com.example.dataset;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DatasetApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private void insert(String dataset, String json) throws Exception {
        mockMvc.perform(post("/api/dataset/" + dataset + "/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    private void seedEmployees(String dataset) throws Exception {
        insert(dataset, "{\"id\":1,\"name\":\"John Doe\",\"age\":30,\"department\":\"Engineering\"}");
        insert(dataset, "{\"id\":2,\"name\":\"Jane Smith\",\"age\":25,\"department\":\"Engineering\"}");
        insert(dataset, "{\"id\":3,\"name\":\"Alice Brown\",\"age\":28,\"department\":\"Marketing\"}");
    }

    @Test
    void insertRecord_returnsConfirmation() throws Exception {
        mockMvc.perform(post("/api/dataset/t_insert/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"name\":\"John Doe\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Record added successfully"))
                .andExpect(jsonPath("$.dataset").value("t_insert"))
                .andExpect(jsonPath("$.recordId").value(1));
    }

    @Test
    void insertRecord_duplicateId_returnsConflict() throws Exception {
        insert("t_dup", "{\"id\":1,\"name\":\"A\"}");
        mockMvc.perform(post("/api/dataset/t_dup/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"name\":\"B\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void insertRecord_emptyObject_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/dataset/t_empty/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void insertRecord_malformedJson_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/dataset/t_bad/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void query_groupBy_groupsRecords() throws Exception {
        seedEmployees("t_group");
        mockMvc.perform(get("/api/dataset/t_group/query").param("groupBy", "department"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupedRecords.Engineering", hasSize(2)))
                .andExpect(jsonPath("$.groupedRecords.Marketing", hasSize(1)));
    }

    @Test
    void query_sortByAsc_sortsNumerically() throws Exception {
        seedEmployees("t_sort_asc");
        mockMvc.perform(get("/api/dataset/t_sort_asc/query").param("sortBy", "age").param("order", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sortedRecords[0].name").value("Jane Smith"))
                .andExpect(jsonPath("$.sortedRecords[2].name").value("John Doe"));
    }

    @Test
    void query_sortByDesc_reversesOrder() throws Exception {
        seedEmployees("t_sort_desc");
        mockMvc.perform(get("/api/dataset/t_sort_desc/query").param("sortBy", "age").param("order", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sortedRecords[0].name").value("John Doe"));
    }

    @Test
    void query_groupByAndSortBy_sortsWithinGroups() throws Exception {
        seedEmployees("t_both");
        mockMvc.perform(get("/api/dataset/t_both/query").param("groupBy", "department").param("sortBy", "age"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupedRecords.Engineering[0].name").value("Jane Smith"));
    }

    @Test
    void query_unknownDataset_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/dataset/does_not_exist/query").param("groupBy", "x"))
                .andExpect(status().isNotFound());
    }

    @Test
    void query_invalidOrder_returnsBadRequest() throws Exception {
        seedEmployees("t_order");
        mockMvc.perform(get("/api/dataset/t_order/query").param("sortBy", "age").param("order", "sideways"))
                .andExpect(status().isBadRequest());
    }
}
