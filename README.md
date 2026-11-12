# JSON Dataset API

A Spring Boot REST service that stores JSON records in named datasets (PostgreSQL) and queries them with **group-by** and **sort-by** operators.

| | |
|---|---|
| **Live API** | https://json-dataset-api-lcn5.onrender.com |
| **Swagger UI** | https://json-dataset-api-lcn5.onrender.com/swagger-ui.html |
| **OpenAPI JSON** | https://json-dataset-api-lcn5.onrender.com/v3/api-docs |
| **Source** | https://github.com/hemaperikala/json-dataset-api |

> The live API runs on Render's free tier, which sleeps when idle. The first request after a pause can take up to a minute.

**Stack:** Java 17, Spring Boot 3.3, Spring Data JPA / Hibernate, PostgreSQL (production, Neon) and H2 (local + tests), springdoc-openapi (Swagger), JUnit 5, Mockito, MockMvc, Docker.

---

## Run locally

Requirements: JDK 17+ and Maven 3.9+.

```powershell
mvn spring-boot:run
```

The app starts on http://localhost:8080 with an in-memory H2 database, so no setup is needed. Swagger UI: http://localhost:8080/swagger-ui.html

### Run against PostgreSQL

Spring Boot reads these standard environment variables:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://HOST/DBNAME?sslmode=require"
$env:SPRING_DATASOURCE_USERNAME="user"
$env:SPRING_DATASOURCE_PASSWORD="password"
mvn spring-boot:run
```

The table is created automatically (`spring.jpa.hibernate.ddl-auto=update`).

### Run with Docker

```powershell
docker build -t dataset-api .
docker run -p 8080:8080 dataset-api
```

## Run the tests

```powershell
mvn test
```

The suite has unit tests (service with a mocked repository, sorter, grouper, sort-order parsing) and integration tests (the full HTTP stack with MockMvc against H2).

## Test with Postman

Import `postman/JSON-Dataset-API.postman_collection.json`. It contains every request below, including the error cases. Change the `baseUrl` collection variable to switch between local and the live API.

---

## API

### 1. Insert a record

`POST /api/dataset/{datasetName}/record` returns **201 Created**

Any JSON object is accepted. If it has an `id` field, the id must be unique inside the dataset.

```json
{ "id": 1, "name": "John Doe", "age": 30, "department": "Engineering" }
```

Response:

```json
{ "message": "Record added successfully", "dataset": "employee_dataset", "recordId": 1 }
```

### 2. Query a dataset

`GET /api/dataset/{datasetName}/query`

| Parameter | Required | Description |
|---|---|---|
| `groupBy` | no | Field to group by, e.g. `department` |
| `sortBy` | no | Field to sort by, e.g. `age` |
| `order` | no | `asc` (default) or `desc` |

| Parameters sent | Response key |
|---|---|
| `groupBy` only | `groupedRecords` |
| `sortBy` (+ `order`) only | `sortedRecords` |
| both | `groupedRecords`, with every group sorted |
| none | `records` (all records in insertion order) |

**Group by** `?groupBy=department`

```json
{
  "groupedRecords": {
    "Engineering": [
      { "id": 1, "name": "John Doe", "age": 30, "department": "Engineering" },
      { "id": 2, "name": "Jane Smith", "age": 25, "department": "Engineering" }
    ],
    "Marketing": [
      { "id": 3, "name": "Alice Brown", "age": 28, "department": "Marketing" }
    ]
  }
}
```

**Sort by** `?sortBy=age&order=asc`

```json
{
  "sortedRecords": [
    { "id": 2, "name": "Jane Smith", "age": 25, "department": "Engineering" },
    { "id": 3, "name": "Alice Brown", "age": 28, "department": "Marketing" },
    { "id": 1, "name": "John Doe", "age": 30, "department": "Engineering" }
  ]
}
```

### Errors

Every error uses the same JSON shape:

```json
{ "timestamp": "2026-11-12T10:14:58Z", "status": 409, "error": "Conflict", "message": "Record with id '1' already exists in dataset 'employee_dataset'" }
```

| Status | When |
|---|---|
| 400 | Malformed JSON, empty body, invalid dataset name, `order` is not `asc` or `desc` |
| 404 | Dataset has no records, or the URL does not exist |
| 409 | A record with the same `id` already exists in the dataset |
| 500 | Unexpected server error (details are logged, never returned) |

---

## Design

```
controller  ->  service  ->  repository  ->  PostgreSQL
                   |
                   +-- RecordJsonMapper   JSON <-> Map conversion
                   +-- RecordSorter       sorting rules
                   +-- RecordGrouper      grouping rules
exception   GlobalExceptionHandler (@RestControllerAdvice) maps exceptions to HTTP responses
```

- **Layered architecture:** controllers only handle HTTP, the service holds the use cases, the repository handles persistence.
- **Single responsibility:** sorting, grouping and JSON conversion each live in their own small, separately tested class, so `DatasetService` stays a thin coordinator.
- **Repository pattern:** Spring Data JPA behind `DatasetRecordRepository`.
- **Centralised exception handling:** domain exceptions (`DatasetNotFoundException`, `DuplicateRecordException`, `InvalidRequestException`) are translated in one place.
- **Dependency injection:** all collaborators come through constructors, which keeps the unit tests simple.

### Assumptions

- Records are free-form JSON objects, stored as a JSON text column keyed by dataset name, so any shape works without schema changes.
- `id` is optional. When present it must be unique within its dataset.
- Numbers sort numerically (10 after 9), text sorts case-insensitively, and records missing the sort field always go last.
- Group keys are the field value as text. Records without the group field are collected under `"null"`.
- Sorting happens before grouping, so each group is already sorted.
- Dataset names may contain letters, digits, `_` and `-` (max 100 characters).

## Deployment

The live service runs from the included `Dockerfile` on Render, connected to a Neon PostgreSQL database through the `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD` environment variables. Secrets are never stored in the repository.
