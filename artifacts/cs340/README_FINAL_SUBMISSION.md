# CS 499 Final Artifact 3 - Databases

## Artifact

**Course:** CS 340 Client/Server Development  
**Artifact:** Grazioso Salvare Austin Animal Center Dashboard  
**Category:** Databases  
**Database:** MongoDB `aac.animals`

## Package contents

- `original/` contains the original CS 340 CRUD module, dashboard notebook, and README.
- `enhanced/` contains the final enhanced MongoDB CRUD/search layer, dashboard files, configuration helper, sample queries, and explain-output generator.
- `tests/` contains the mocked unit tests and optional temporary MongoDB integration test.
- `docs/` contains the database enhancement README, test output, and explain-output generation instructions.

## Final polish focus

The Milestone Four enhancement already included allowlisted MongoDB queries, result caps, safer writes, environment-based configuration, index support, sample queries, error cases, and nine passing mocked tests. The final polish adds the requested temporary MongoDB integration test and `explain()` evidence workflow for the Water Rescue filter.

## Most relevant enhanced files

- `enhanced/CRUD_Python_Module_Enhanced.py`
- `enhanced/database_config.py`
- `enhanced/sample_queries.py`
- `enhanced/generate_explain_output.py`
- `tests/test_animal_shelter_enhanced.py`
- `tests/test_mongo_integration.py`
- `docs/README_Milestone_Four_Database_Enhancement.md`
- `docs/TEST_OUTPUT_FINAL.txt`
- `docs/explain_water_rescue_GENERATE_LOCALLY.md`

## Final local MongoDB command needed before GitHub Pages upload

The container used to prepare this package does not have PyMongo or a running MongoDB server. To generate the final `explain()` JSON output in Codio, Jupyter, or a local MongoDB environment, run:

```bash
export AAC_MONGO_TEST_URI='mongodb://localhost:27017'
python enhanced/generate_explain_output.py
```

or run the integration test directly:

```bash
export AAC_RUN_MONGO_INTEGRATION=1
export AAC_MONGO_TEST_URI='mongodb://localhost:27017'
python -m unittest tests.test_mongo_integration -v
```

This creates a temporary database, inserts sample animal records, creates the `idx_rescue_filter` compound index, runs Water Rescue `explain()` output, writes `docs/explain_water_rescue.json`, and drops the temporary database.
