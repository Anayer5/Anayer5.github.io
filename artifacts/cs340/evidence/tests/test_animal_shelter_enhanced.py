import unittest

from enhanced.CRUD_Python_Module_Enhanced import AnimalShelter, QueryValidationError


class Result:
    def __init__(self, acknowledged=True, modified_count=0, deleted_count=0):
        self.acknowledged = acknowledged
        self.modified_count = modified_count
        self.deleted_count = deleted_count


class FakeCursor(list):
    def sort(self, sort_spec):
        field, direction = sort_spec[0]
        reverse = direction == -1
        return FakeCursor(sorted(self, key=lambda item: item.get(field), reverse=reverse))

    def limit(self, limit):
        return FakeCursor(self[:limit])


class FakeCollection:
    def __init__(self, records):
        self.records = [dict(record) for record in records]
        self.last_query = None

    def find(self, query, projection=None):
        self.last_query = query
        return FakeCursor([record for record in self.records if self._matches(record, query)])

    def insert_one(self, data):
        self.records.append(dict(data))
        return Result(acknowledged=True)

    def update_many(self, query, update_doc):
        return self._update(query, update_doc)

    def update_one(self, query, update_doc):
        return self._update(query, update_doc, first_only=True)

    def delete_many(self, query):
        before = len(self.records)
        self.records = [record for record in self.records if not self._matches(record, query)]
        return Result(deleted_count=before - len(self.records))

    def delete_one(self, query):
        for index, record in enumerate(self.records):
            if self._matches(record, query):
                del self.records[index]
                return Result(deleted_count=1)
        return Result(deleted_count=0)

    def count_documents(self, query):
        return len([record for record in self.records if self._matches(record, query)])

    def _update(self, query, update_doc, first_only=False):
        count = 0
        for record in self.records:
            if self._matches(record, query):
                for field, value in update_doc.get("$set", {}).items():
                    record[field] = value
                for field in update_doc.get("$unset", {}).keys():
                    record.pop(field, None)
                count += 1
                if first_only:
                    break
        return Result(modified_count=count)

    def _matches(self, record, query):
        if not query:
            return True
        for key, value in query.items():
            if key == "$or":
                return any(self._matches(record, clause) for clause in value)
            if key == "$and":
                return all(self._matches(record, clause) for clause in value)
            if isinstance(value, dict):
                current = record.get(key)
                for op, op_value in value.items():
                    if op == "$options":
                        continue
                    if op == "$gte" and not (current >= op_value):
                        return False
                    if op == "$lte" and not (current <= op_value):
                        return False
                    if op == "$regex" and op_value.lower().replace("\\", "") not in str(current).lower():
                        return False
                continue
            if record.get(key) != value:
                return False
        return True


class AnimalShelterEnhancedTests(unittest.TestCase):
    def setUp(self):
        self.shelter = AnimalShelter.__new__(AnimalShelter)
        self.shelter.default_limit = 2
        self.shelter.max_limit = 5
        self.shelter.collection = FakeCollection(
            [
                {
                    "animal_id": "A1",
                    "animal_type": "Dog",
                    "sex_upon_outcome": "Intact Female",
                    "age_upon_outcome_in_weeks": 52,
                    "breed": "Labrador Retriever Mix",
                    "name": "Daisy",
                    "location_lat": 30.1,
                    "location_long": -97.1,
                },
                {
                    "animal_id": "A2",
                    "animal_type": "Dog",
                    "sex_upon_outcome": "Intact Male",
                    "age_upon_outcome_in_weeks": 80,
                    "breed": "German Shepherd",
                    "name": "Max",
                    "location_lat": 30.2,
                    "location_long": -97.2,
                },
                {
                    "animal_id": "A3",
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

    def test_read_applies_default_limit(self):
        records = self.shelter.read({})
        self.assertEqual(len(records), 2)

    def test_read_rejects_unapproved_query_field(self):
        with self.assertRaises(QueryValidationError):
            self.shelter.read({"$where": "this.password == 'x'"})

    def test_water_rescue_query_returns_matching_dog(self):
        records = self.shelter.find_rescue_candidates("water", limit=5)
        self.assertEqual(len(records), 1)
        self.assertEqual(records[0]["animal_id"], "A1")

    def test_update_blocks_protected_id_field(self):
        with self.assertRaises(QueryValidationError):
            self.shelter.update({"animal_id": "A1"}, {"animal_id": "NEW"})

    def test_update_allows_safe_field_change(self):
        modified = self.shelter.update({"animal_id": "A1"}, {"name": "Daisy Updated"})
        self.assertEqual(modified, 1)
        records = self.shelter.read({"animal_id": "A1"}, limit=1)
        self.assertEqual(records[0]["name"], "Daisy Updated")

    def test_delete_requires_explicit_confirmation(self):
        with self.assertRaises(QueryValidationError):
            self.shelter.delete({"animal_id": "A1"})

    def test_delete_with_confirmation(self):
        deleted = self.shelter.delete({"animal_id": "A3"}, confirm=True)
        self.assertEqual(deleted, 1)

    def test_update_defaults_to_one_document(self):
        self.shelter.collection.records.append({
            "animal_id": "A4",
            "animal_type": "Dog",
            "sex_upon_outcome": "Intact Female",
            "age_upon_outcome_in_weeks": 60,
            "breed": "Labrador Retriever Mix",
            "name": "Daisy",
            "location_lat": 30.4,
            "location_long": -97.4,
        })
        modified = self.shelter.update({"name": "Daisy"}, {"color": "Brown"})
        self.assertEqual(modified, 1)

    def test_delete_defaults_to_one_document(self):
        self.shelter.collection.records.append({
            "animal_id": "A4",
            "animal_type": "Cat",
            "sex_upon_outcome": "Intact Female",
            "age_upon_outcome_in_weeks": 60,
            "breed": "Domestic Shorthair",
            "name": "Misty",
            "location_lat": 30.4,
            "location_long": -97.4,
        })
        deleted = self.shelter.delete({"animal_type": "Cat"}, confirm=True)
        self.assertEqual(deleted, 1)
        remaining_cats = self.shelter.read({"animal_type": "Cat"}, limit=5)
        self.assertEqual(len(remaining_cats), 1)


if __name__ == "__main__":
    unittest.main()
