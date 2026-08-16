# CS 499 Milestone Four: Enhancement Three - Databases

## Artifact

**Course:** CS 340: Client/Server Development  
**Artifact:** Grazioso Salvare Austin Animal Center Dashboard  
**Database:** MongoDB  
**Database name:** `aac`  
**Collection name:** `animals`

The original artifact used a reusable Python CRUD module to connect a Dash/JupyterDash dashboard to MongoDB. The dashboard provided rescue filters for Water Rescue, Mountain or Wilderness Rescue, Disaster or Individual Tracking, and Reset. It also displayed matching records in a data table, summarized breed distribution, and mapped the selected animal's location.

## Original Files Included

- `original/CRUD_Python_Module_original.py`
- `original/ProjectTwoDashboard_original.ipynb`
- `original/README_original.md`

## Enhanced Files Included

- `enhanced/database_config.py`
- `enhanced/CRUD_Python_Module_Enhanced.py`
- `enhanced/ProjectTwoDashboard_Enhanced.py`
- `enhanced/ProjectTwoDashboard_Enhanced.ipynb`
- `enhanced/sample_queries.py`
- `tests/test_animal_shelter_enhanced.py`
- `docs/TEST_OUTPUT.txt`

## Instructor Feedback Addressed

The Milestone One feedback requested stronger evidence for the database artifact, including the actual MongoDB collection, current CRUD method signatures, hard-coded configuration, current filters, error behavior, exact planned changes, allowed query fields, result limits, sample queries, exception behavior, configuration variables, and named test cases. This enhancement addresses those items directly.

## Current Database Evidence From the Original Artifact

### Actual MongoDB database and collection

The enhanced artifact documents and uses:

```text
Database: aac
Collection: animals
Authentication source: admin
Default user: aacuser
```

### Original CRUD method signatures

The original artifact used an `AnimalShelter` class with these CRUD methods:

```python
create(data: Dict) -> bool
read(query: Optional[Dict] = None) -> List[Dict]
update(query: Dict, new_values: Dict) -> int
delete(query: Dict) -> int
close() -> None
```

### Original dashboard filters

The dashboard used these filter choices:

```text
water     -> Water Rescue
mountain  -> Mountain or Wilderness Rescue
disaster  -> Disaster or Individual Tracking
reset     -> Reset/full dashboard state
```

### Original hard-coded configuration issue

The original dashboard notebook included the username and password directly in the notebook:

```python
USERNAME = "aacuser"
PASSWORD = "Passw0rd"
shelter = AnimalShelter(username=USERNAME, password=PASSWORD)
```

The enhanced dashboard removes this credential from the notebook and reads configuration from environment variables.

## Database Enhancements Performed

### 1. Environment-based configuration

The new `database_config.py` file centralizes MongoDB configuration and reads sensitive information from environment variables.

Supported variables:

```text
AAC_MONGO_USERNAME
AAC_MONGO_PASSWORD
AAC_MONGO_HOST
AAC_MONGO_PORT
AAC_MONGO_DB
AAC_MONGO_COLLECTION
AAC_MONGO_AUTH_SOURCE
AAC_DEFAULT_LIMIT
AAC_MAX_LIMIT
```

`AAC_MONGO_PASSWORD` is required. This prevents committing a live MongoDB password to the repository.

### 2. Query allowlisting

The enhanced CRUD module validates query fields before sending the query to MongoDB. This reduces the risk of unsafe or unexpected query documents being passed from the dashboard layer.

Allowed query fields include:

```text
_id, rec_num, animal_id, animal_type, breed, color, date_of_birth,
datetime, monthyear, name, outcome_subtype, outcome_type,
sex_upon_outcome, age_upon_outcome, age_upon_outcome_in_weeks,
location_lat, location_long
```

Allowed logical operators:

```text
$or, $and
```

Allowed comparison/search operators:

```text
$eq, $ne, $gt, $gte, $lt, $lte, $in, $nin, $regex, $options
```

### 3. Result limits

The enhanced `read()` method enforces result limits. This avoids returning the entire `aac.animals` collection by accident and makes the dashboard safer and more scalable.

```python
read(query=None, limit=None, projection=None, sort=None) -> List[Dict]
```

If no limit is provided, the module uses `default_limit`. If the caller requests too many records, the module caps the request at `max_limit`.

### 4. Safer CRUD behavior

The enhanced module preserves recognizable CRUD behavior while adding validation and clearer exception behavior.

Enhanced signatures:

```python
create(data: Dict[str, Any]) -> bool
read(query=None, limit=None, projection=None, sort=None) -> List[Dict[str, Any]]
update(query: Dict[str, Any], new_values: Dict[str, Any], many=False) -> int
delete(query: Dict[str, Any], confirm=False, many=False) -> int
count(query=None) -> int
create_indexes() -> List[str]
find_rescue_candidates(rescue_type: str, limit=None) -> List[Dict[str, Any]]
build_rescue_query(rescue_type: str) -> Dict[str, Any]
```

### 5. Safer update and delete operations

Updates require a non-empty query and only allow approved update fields. Protected identity fields, such as `_id` and `animal_id`, cannot be changed through the enhanced update method. The enhanced method updates one document by default. Bulk update requires the caller to explicitly pass `many=True`.

Delete operations require both a non-empty query and explicit confirmation. The enhanced method deletes one document by default. Bulk delete requires the caller to explicitly pass `many=True`:

```python
delete({"animal_id": "A123456"}, confirm=True, many=False)
```

This protects the collection from accidental broad deletes.

### 6. Vetted rescue query methods

The enhanced module includes `find_rescue_candidates()` and `build_rescue_query()` so the dashboard does not have to construct raw MongoDB queries directly. This centralizes rescue filter logic in the database layer and makes the dashboard easier to maintain.

### 7. Index support

The enhanced module adds `create_indexes()` for fields commonly used by the dashboard:

```text
animal_type
sex_upon_outcome
breed
age_upon_outcome_in_weeks
location_lat + location_long
```

This documents performance-oriented database design and supports faster filtering in MongoDB.

## How to Run the Enhanced Dashboard

Set the required environment variable first:

```bash
export AAC_MONGO_PASSWORD='<your local MongoDB password>'
```

Optional environment variables may also be set:

```bash
export AAC_MONGO_USERNAME='aacuser'
export AAC_MONGO_HOST='localhost'
export AAC_MONGO_PORT='27017'
export AAC_MONGO_DB='aac'
export AAC_MONGO_COLLECTION='animals'
export AAC_MONGO_AUTH_SOURCE='admin'
export AAC_DEFAULT_LIMIT='100'
export AAC_MAX_LIMIT='500'
```

Then run either:

```bash
python enhanced/sample_queries.py
```

or open:

```text
enhanced/ProjectTwoDashboard_Enhanced.ipynb
```

## Unit Tests

The unit tests use a fake collection so they can be run without a live MongoDB server. This makes the validation logic verifiable even when the original Codio/Jupyter MongoDB environment is unavailable.

Run tests from the project root:

```bash
python -m unittest discover -s tests -v
```

Test cases included:

```text
test_read_applies_default_limit
test_read_rejects_unapproved_query_field
test_water_rescue_query_returns_matching_dog
test_update_blocks_protected_id_field
test_update_allows_safe_field_change
test_update_defaults_to_one_document
test_delete_requires_explicit_confirmation
test_delete_with_confirmation
test_delete_defaults_to_one_document
```

Current verification output is stored in:

```text
docs/TEST_OUTPUT.txt
```

## Limitations and Final ePortfolio Notes

The unit tests verify validation, query safety, result limiting, update protections, one-document update/delete defaults, and delete confirmation with fake data. A final manual test should still be completed in the original MongoDB/Jupyter environment by connecting to the real `aac.animals` collection and confirming that the dashboard loads correctly for Reset, Water Rescue, Mountain or Wilderness Rescue, and Disaster or Individual Tracking.

---

## Final ePortfolio polish after instructor feedback

The Milestone Four feedback requested two additional pieces of evidence before the final ePortfolio:

1. Add one test against a temporary MongoDB instance.
2. Include `explain()` output for a common rescue filter so index behavior is demonstrated beyond the mocked tests.

### Final polish additions

- Added `AnimalShelter.from_existing_collection()` so the enhanced CRUD layer can be tested against a temporary MongoDB collection without hard-coding credentials.
- Added `AnimalShelter.explain_rescue_filter()` to return MongoDB `explain()` output for a vetted rescue filter.
- Added the compound index `idx_rescue_filter` over `animal_type`, `sex_upon_outcome`, `age_upon_outcome_in_weeks`, and `breed`.
- Added `tests/test_mongo_integration.py`, an optional integration test that creates a temporary MongoDB database, inserts sample AAC-style animal records, verifies the Water Rescue filter against a real MongoDB collection, writes `docs/explain_water_rescue.json`, and drops the temporary database.
- Added `enhanced/generate_explain_output.py` so the same `explain()` evidence can be generated directly from a local or Codio MongoDB instance.
- Added `docs/explain_water_rescue_GENERATE_LOCALLY.md` with the exact command sequence for generating the final explain output.

### Running the final mocked and integration tests

The mocked tests still run without MongoDB:

```bash
python -m unittest discover -s tests -v
```

The integration test requires PyMongo and a reachable MongoDB instance:

```bash
export AAC_RUN_MONGO_INTEGRATION=1
export AAC_MONGO_TEST_URI='mongodb://localhost:27017'
python -m unittest tests.test_mongo_integration -v
```

The integration test writes:

```text
docs/explain_water_rescue.json
```

### Current verification status

This container does not include PyMongo or a running MongoDB server, so the temporary MongoDB integration test is included but skipped here. The final mocked validation tests pass, and the integration test is ready to run in the original CS 340 MongoDB/Codio/Jupyter environment.

Current local output is stored in:

```text
docs/TEST_OUTPUT_FINAL.txt
```

