package com.example.dataset.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/** One JSON record belonging to a named dataset. The JSON itself is stored as text. */
@Entity
@Table(name = "dataset_records", indexes = @Index(name = "idx_dataset_name", columnList = "dataset_name"))
public class DatasetRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dataset_name", nullable = false, length = 100)
    private String datasetName;

    /** The "id" field supplied inside the JSON body (nullable if the client did not send one). */
    @Column(name = "record_id", length = 100)
    private String recordId;

    @Column(name = "data", nullable = false, columnDefinition = "TEXT")
    private String data;

    protected DatasetRecord() {
        // required by JPA
    }

    public DatasetRecord(String datasetName, String recordId, String data) {
        this.datasetName = datasetName;
        this.recordId = recordId;
        this.data = data;
    }

    public Long getId() { return id; }
    public String getDatasetName() { return datasetName; }
    public String getRecordId() { return recordId; }
    public String getData() { return data; }
}
