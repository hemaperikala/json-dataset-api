# JSON Dataset API

Spring Boot service to store JSON records in named datasets and query them with **group-by** and **sort-by** operators.

**Stack:** Java 17, Spring Boot 3.3, Spring Data JPA, PostgreSQL (prod) / H2 (local + tests), springdoc-openapi (Swagger), JUnit 5 + MockMvc.

## Run locally (no setup needed, uses in-memory H2)
```powershell
mvn spring-boot:run
```
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Run tests
```powershell
mvn test
```

## Run against PostgreSQL
```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://HOST/DBNAME?sslmode=require"
$env:SPRING_DATASOURCE_USERNAME="user"
$env:SPRING_DATASOURCE_PASSWORD="password"
mvn spring-boot:run
```

## API

### Insert record
`POST /api/dataset/{datasetName}/record` -> `201 Created`
```json
{ "id": 1, "name": "John Doe", "age": 30, "department": "Engineering" }
```
```json
{ "message": "Record added successfully", "dataset": "employee_dataset", "recordId": 1 }
```

### Query
`GET /api/dataset/{datasetName}/query?groupBy=department&sortBy=age&order=asc`

| Params | Response key |
|---|---|
| `groupBy` only | `groupedRecords` |
| `sortBy` (+`order`) only | `sortedRecords` |
| both | `groupedRecords`, each group sorted |
| none | `records` (all, insertion order) |

`order` is `asc` (default) or `desc`. Numbers sort numerically, text case-insensitively, nulls last.

### Errors
All errors return `{ timestamp, status, error, message }`.

| Status | When |
|---|---|
| 400 | malformed JSON, empty body, invalid dataset name, invalid `order` |
| 404 | dataset has no records |
| 409 | duplicate `id` inside the same dataset |
| 500 | unexpected error |

## Design notes
- Layers: controller -> service -> repository, with a `@RestControllerAdvice` for errors.
- Each record is stored as a JSON text column keyed by dataset name, so any shape of JSON works without schema changes.
- Sorting is done first, then grouping keeps insertion order, so sorted groups come for free.
