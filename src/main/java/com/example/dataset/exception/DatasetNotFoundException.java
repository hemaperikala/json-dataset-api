package com.example.dataset.exception;

public class DatasetNotFoundException extends RuntimeException {
    public DatasetNotFoundException(String dataset) {
        super("Dataset '" + dataset + "' not found or has no records");
    }
}
