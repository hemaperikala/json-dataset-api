package com.example.dataset.exception;

public class DuplicateRecordException extends RuntimeException {
    public DuplicateRecordException(String dataset, String recordId) {
        super("Record with id '" + recordId + "' already exists in dataset '" + dataset + "'");
    }
}
