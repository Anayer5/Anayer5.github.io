# Water Rescue explain() Output - Local Generation Required

The final ePortfolio feedback requested `explain()` output for a common rescue filter. This environment does not include a running MongoDB server, so the actual `docs/explain_water_rescue.json` file must be generated in Codio, Jupyter, or a local development environment where MongoDB is available.

Run one of the following commands from the project root:

```bash
export AAC_MONGO_TEST_URI='mongodb://localhost:27017'
python enhanced/generate_explain_output.py
```

or:

```bash
export AAC_RUN_MONGO_INTEGRATION=1
export AAC_MONGO_TEST_URI='mongodb://localhost:27017'
python -m unittest tests.test_mongo_integration -v
```

The command creates a temporary MongoDB database, inserts sample animal records, creates the `idx_rescue_filter` compound index, runs the Water Rescue filter with `explain()`, writes `docs/explain_water_rescue.json`, and drops the temporary database.

Expected evidence to mention in the ePortfolio:

- The query is the Water Rescue filter.
- The index name is `idx_rescue_filter`.
- The output should contain `idx_rescue_filter` and `IXSCAN` when MongoDB uses the hinted index.
