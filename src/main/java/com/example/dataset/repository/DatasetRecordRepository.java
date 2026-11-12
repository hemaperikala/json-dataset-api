package com.example.dataset.repository;

import com.example.dataset.model.DatasetRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetRecordRepository extends JpaRepository<DatasetRecord, Long> {

    List<DatasetRecord> findByDatasetNameOrderByIdAsc(String datasetName);

    boolean existsByDatasetNameAndRecordId(String datasetName, String recordId);
}
