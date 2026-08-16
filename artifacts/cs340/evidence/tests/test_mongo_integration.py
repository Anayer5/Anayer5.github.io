"""Optional integration test for a temporary MongoDB collection.

Run only when a local or course MongoDB instance is available:

    export AAC_RUN_MONGO_INTEGRATION=1
    export AAC_MONGO_TEST_URI='mongodb://localhost:27017'
    python -m unittest tests.test_mongo_integration -v

The test creates a temporary database, inserts a small set of animal records,
creates the same rescue-filter index used by the enhanced data layer, verifies
Water Rescue search behavior against a real MongoDB collection, writes explain()
output for the Water Rescue filter, and then drops the temporary database.
"""

from __future__ import annotations

import json
import os
import unittest
import uuid
from pathlib import Path

from enhanced.CRUD_Python_Module_Enhanced import AnimalShelter

try:
    from pymongo import ASCENDING, MongoClient
except ImportError:  # pragma: no cover - environment-dependent optional test
    ASCENDING = 1
    MongoClient = None


@unittest.skipUnless(
    os.environ.get("AAC_RUN_MONGO_INTEGRATION") == "1" and MongoClient is not None,
    "Set AAC_RUN_MONGO_INTEGRATION=1 and install pymongo to run MongoDB integration tests.",
)
class MongoIntegrationTests(unittest.TestCase):
    """Integration tests that require a real temporary MongoDB database."""

    @classmethod
    def setUpClass(cls):
        cls.mongo_uri = os.environ.get("AAC_MONGO_TEST_URI", "mongodb://localhost:27017")
        cls.client = MongoClient(cls.mongo_uri, serverSelectionTimeoutMS=5000)
        try:
            cls.client.admin.command("ping")
        except Exception as exc:  # pragma: no cover - depends on external service
            raise unittest.SkipTest(f"Temporary MongoDB instance is not reachable: {exc}")

        cls.db_name = f"cs499_temp_aac_{uuid.uuid4().hex[:10]}"
        cls.collection = cls.client[cls.db_name]["animals"]
        cls.shelter = AnimalShelter.from_existing_collection(cls.collection, default_limit=100, max_limit=500)

        cls.collection.insert_many(
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
                {
                    "animal_id": "TEMP_CAT_1",
                    "animal_type": "Cat",
                    "sex_upon_outcome": "Intact Female",
                    "age_upon_outcome_in_weeks": 40,
                    "breed": "Domestic Shorthair",
                    "name": "Misty",
                    "location_lat": 30.3,
                    "location_long": -97.3,
                },
            ]
        )
        cls.collection.create_index(
            [
                ("animal_type", ASCENDING),
                ("sex_upon_outcome", ASCENDING),
                ("age_upon_outcome_in_weeks", ASCENDING),
                ("breed", ASCENDING),
            ],
            name="idx_rescue_filter",
        )

    @classmethod
    def tearDownClass(cls):
        cls.client.drop_database(cls.db_name)
        cls.client.close()

    def test_water_rescue_filter_against_temporary_mongodb(self):
        records = self.shelter.find_rescue_candidates("water", limit=10)

        self.assertEqual(1, len(records))
        self.assertEqual("TEMP_WATER_1", records[0]["animal_id"])

    def test_water_rescue_explain_output_is_written(self):
        explain_output = self.shelter.explain_rescue_filter(
            "water",
            limit=10,
            hint="idx_rescue_filter",
        )

        output_path = Path(__file__).resolve().parents[1] / "docs" / "explain_water_rescue.json"
        output_path.write_text(json.dumps(explain_output, indent=2, default=str), encoding="utf-8")

        serialized = json.dumps(explain_output, default=str)
        self.assertIn("idx_rescue_filter", serialized)


if __name__ == "__main__":
    unittest.main()
