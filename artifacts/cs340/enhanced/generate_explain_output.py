"""Generate explain() evidence for the Water Rescue dashboard filter.

This script requires a reachable MongoDB instance and PyMongo. It creates a
temporary database, inserts sample Austin Animal Center-style records, creates the
rescue-filter index, runs explain() for the Water Rescue filter, writes the JSON
output to docs/explain_water_rescue.json, and then drops the temporary database.

Usage:
    export AAC_MONGO_TEST_URI='mongodb://localhost:27017'
    python enhanced/generate_explain_output.py
"""

from __future__ import annotations

import json
import os
import sys
import uuid
from pathlib import Path

try:
    from pymongo import ASCENDING, MongoClient
except ImportError as exc:
    raise SystemExit("pymongo is required to generate explain() output.") from exc

from CRUD_Python_Module_Enhanced import AnimalShelter


def main() -> int:
    mongo_uri = os.environ.get("AAC_MONGO_TEST_URI", "mongodb://localhost:27017")
    client = MongoClient(mongo_uri, serverSelectionTimeoutMS=5000)
    client.admin.command("ping")

    db_name = f"cs499_temp_aac_{uuid.uuid4().hex[:10]}"
    collection = client[db_name]["animals"]

    try:
        collection.insert_many(
            [
                {
                    "animal_id": "TEMP_WATER_1",
                    "animal_type": "Dog",
                    "sex_upon_outcome": "Intact Female",
                    "age_upon_outcome_in_weeks": 52,
                    "breed": "Labrador Retriever Mix",
                    "name": "Daisy",
                    "location_lat": 30.1,
                    "location_long": -97.1,
                },
                {
                    "animal_id": "TEMP_MOUNTAIN_1",
                    "animal_type": "Dog",
                    "sex_upon_outcome": "Intact Male",
                    "age_upon_outcome_in_weeks": 80,
                    "breed": "German Shepherd",
                    "name": "Max",
                    "location_lat": 30.2,
                    "location_long": -97.2,
                },
            ]
        )
        collection.create_index(
            [
                ("animal_type", ASCENDING),
                ("sex_upon_outcome", ASCENDING),
                ("age_upon_outcome_in_weeks", ASCENDING),
                ("breed", ASCENDING),
            ],
            name="idx_rescue_filter",
        )

        shelter = AnimalShelter.from_existing_collection(collection, default_limit=100, max_limit=500)
        explain_output = shelter.explain_rescue_filter("water", limit=10, hint="idx_rescue_filter")

        output_path = Path(__file__).resolve().parents[1] / "docs" / "explain_water_rescue.json"
        output_path.write_text(json.dumps(explain_output, indent=2, default=str), encoding="utf-8")
        print(f"Wrote explain output to {output_path}")
        return 0
    finally:
        client.drop_database(db_name)
        client.close()


if __name__ == "__main__":
    sys.exit(main())
