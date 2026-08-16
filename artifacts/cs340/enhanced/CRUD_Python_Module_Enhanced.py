"""
Enhanced MongoDB CRUD module for the CS 340 Grazioso Salvare dashboard.

Enhancement goals:
    1. Remove hard-coded credentials from the dashboard/database layer.
    2. Document the actual MongoDB database and collection: aac.animals.
    3. Add query-field allowlisting and operator validation.
    4. Add result limits so the dashboard does not unintentionally load the full collection.
    5. Add safer create, read, update, and delete behavior with clear exception types.
    6. Add vetted rescue-candidate query builders for the three dashboard filters.
    7. Add optional index creation for common filter fields.
    8. Add integration-test support and explain() evidence for common rescue filters.

The public CRUD signatures remain recognizable from the original artifact:
    create(data) -> bool
    read(query=None, limit=None, projection=None, sort=None) -> list[dict]
    update(query, new_values, many=False) -> int
    delete(query, confirm=False, many=False) -> int
"""

from __future__ import annotations

import logging
import re
from typing import Any, Dict, Iterable, List, Mapping, Optional, Sequence, Tuple
from urllib.parse import quote_plus

try:
    from database_config import MongoSettings
except ImportError:  # Allows package-style imports during unit testing.
    from .database_config import MongoSettings

try:  # PyMongo is available in Codio/Jupyter, but may be absent in local test runners.
    from pymongo import ASCENDING, MongoClient
    from pymongo.errors import PyMongoError
except ImportError:  # Allows unit tests with fake collections to run without MongoDB/PyMongo.
    ASCENDING = 1
    MongoClient = None

    class PyMongoError(Exception):
        """Fallback exception used only when PyMongo is not installed."""


LOGGER = logging.getLogger(__name__)


class QueryValidationError(ValueError):
    """Raised when a query or update document contains unsafe or unsupported content."""


class DatabaseOperationError(RuntimeError):
    """Raised when an expected database operation fails."""


class AnimalShelter(object):
    """Secure CRUD and search operations for the MongoDB aac.animals collection."""

    # Fields observed in the Austin Animal Center Outcomes data and used by the dashboard.
    ALLOWED_QUERY_FIELDS = frozenset(
        {
            "_id",
            "rec_num",
            "animal_id",
            "animal_type",
            "breed",
            "color",
            "date_of_birth",
            "datetime",
            "monthyear",
            "name",
            "outcome_subtype",
            "outcome_type",
            "sex_upon_outcome",
            "age_upon_outcome",
            "age_upon_outcome_in_weeks",
            "location_lat",
            "location_long",
        }
    )

    # Only data fields may be changed. Identity fields and MongoDB _id are intentionally excluded.
    ALLOWED_UPDATE_FIELDS = frozenset(
        {
            "animal_type",
            "breed",
            "color",
            "date_of_birth",
            "datetime",
            "monthyear",
            "name",
            "outcome_subtype",
            "outcome_type",
            "sex_upon_outcome",
            "age_upon_outcome",
            "age_upon_outcome_in_weeks",
            "location_lat",
            "location_long",
        }
    )

    ALLOWED_OPERATORS = frozenset(
        {"$eq", "$ne", "$gt", "$gte", "$lt", "$lte", "$in", "$nin", "$regex", "$options"}
    )
    ALLOWED_LOGICAL_OPERATORS = frozenset({"$or", "$and"})
    ALLOWED_UPDATE_OPERATORS = frozenset({"$set", "$unset"})
    ALLOWED_SORT_DIRECTIONS = frozenset({1, -1, ASCENDING})

    RESCUE_TYPES = frozenset({"water", "mountain", "disaster", "reset"})

    def __init__(
        self,
        username: str = "aacuser",
        password: Optional[str] = None,
        host: str = "localhost",
        port: int = 27017,
        db_name: str = "aac",
        collection_name: str = "animals",
        auth_source: str = "admin",
        default_limit: int = 100,
        max_limit: int = 500,
        settings: Optional[MongoSettings] = None,
    ) -> None:
        """
        Create and verify a MongoDB connection.

        The selected collection is the lowercase CS 340 database and collection:
        database = aac, collection = animals.
        """
        if settings is not None:
            username = settings.username
            password = settings.password
            host = settings.host
            port = settings.port
            db_name = settings.db_name
            collection_name = settings.collection_name
            auth_source = settings.auth_source
            default_limit = settings.default_limit
            max_limit = settings.max_limit

        if MongoClient is None:
            raise ImportError("pymongo must be installed to connect to MongoDB.")

        if not password:
            raise ValueError("A MongoDB password must be supplied or AAC_MONGO_PASSWORD must be set.")

        self.default_limit = self._validate_limit(default_limit, fallback=100, max_limit=max_limit)
        self.max_limit = max_limit

        safe_username = quote_plus(username)
        safe_password = quote_plus(password)
        uri = (
            f"mongodb://{safe_username}:{safe_password}"
            f"@{host}:{port}/{db_name}?authSource={auth_source}"
        )

        try:
            self.client = MongoClient(uri, serverSelectionTimeoutMS=5000)
            self.client.admin.command("ping")
            self.database = self.client[db_name]
            self.collection = self.database[collection_name]
        except PyMongoError as exc:
            raise ConnectionError(
                "MongoDB connection failed. Verify that MongoDB is running, "
                "that the lowercase 'aac' database exists, that the 'animals' collection exists, "
                "and that 'aacuser' has readWrite access through the configured authSource."
            ) from exc

    @classmethod
    def from_env(cls) -> "AnimalShelter":
        """Create an AnimalShelter object from environment variables."""
        return cls(settings=MongoSettings.from_env())

    @classmethod
    def from_existing_collection(
        cls,
        collection: Any,
        default_limit: int = 100,
        max_limit: int = 500,
    ) -> "AnimalShelter":
        """
        Create an AnimalShelter wrapper around an existing MongoDB collection.

        This factory is used for integration tests that create a temporary MongoDB
        database/collection, and it also makes the data-access class easier to
        reuse in notebooks or scripts where a client already exists.
        """
        obj = cls.__new__(cls)
        obj.client = getattr(getattr(collection, "database", None), "client", None)
        obj.database = getattr(collection, "database", None)
        obj.collection = collection
        obj.default_limit = cls._validate_limit(default_limit, fallback=100, max_limit=max_limit)
        obj.max_limit = max_limit
        return obj

    def create(self, data: Dict[str, Any]) -> bool:
        """Insert one validated animal document into aac.animals."""
        clean_data = self._validate_insert_document(data)

        try:
            result = self.collection.insert_one(clean_data)
            return bool(result.acknowledged)
        except PyMongoError as exc:
            LOGGER.exception("Create operation failed.")
            raise DatabaseOperationError("Create operation failed.") from exc

    def read(
        self,
        query: Optional[Dict[str, Any]] = None,
        limit: Optional[int] = None,
        projection: Optional[Iterable[str]] = None,
        sort: Optional[Sequence[Tuple[str, int]]] = None,
    ) -> List[Dict[str, Any]]:
        """
        Return matching records using a validated query and enforced result limit.

        Empty query documents are allowed for the dashboard reset state, but the result
        set is still limited by default_limit to avoid unintentionally scanning and
        returning the full collection.
        """
        clean_query = self._validate_query(query or {})
        clean_projection = self._build_projection(projection)
        clean_sort = self._validate_sort(sort)
        effective_limit = self._validate_limit(limit, fallback=self.default_limit, max_limit=self.max_limit)

        try:
            cursor = self.collection.find(clean_query, clean_projection)
            if clean_sort:
                cursor = cursor.sort(clean_sort)
            cursor = cursor.limit(effective_limit)
            return list(cursor)
        except PyMongoError as exc:
            LOGGER.exception("Read operation failed.")
            raise DatabaseOperationError("Read operation failed.") from exc

    def update(self, query: Dict[str, Any], new_values: Dict[str, Any], many: bool = False) -> int:
        """Update matching records using a validated non-empty query and safe update fields."""
        clean_query = self._validate_non_empty_query(query)
        clean_update = self._validate_update_document(new_values)

        try:
            if many:
                result = self.collection.update_many(clean_query, clean_update)
            else:
                result = self.collection.update_one(clean_query, clean_update)
            return int(result.modified_count)
        except PyMongoError as exc:
            LOGGER.exception("Update operation failed.")
            raise DatabaseOperationError("Update operation failed.") from exc

    def delete(self, query: Dict[str, Any], confirm: bool = False, many: bool = False) -> int:
        """
        Delete matching records only when the caller explicitly confirms the operation.

        This prevents accidental dashboard or notebook deletes caused by an incomplete
        query document. The query must also be non-empty.
        """
        if not confirm:
            raise QueryValidationError("Delete operations require confirm=True.")

        clean_query = self._validate_non_empty_query(query)

        try:
            if many:
                result = self.collection.delete_many(clean_query)
            else:
                result = self.collection.delete_one(clean_query)
            return int(result.deleted_count)
        except PyMongoError as exc:
            LOGGER.exception("Delete operation failed.")
            raise DatabaseOperationError("Delete operation failed.") from exc

    def count(self, query: Optional[Dict[str, Any]] = None) -> int:
        """Return the number of records matching a validated query."""
        clean_query = self._validate_query(query or {})
        try:
            return int(self.collection.count_documents(clean_query))
        except PyMongoError as exc:
            LOGGER.exception("Count operation failed.")
            raise DatabaseOperationError("Count operation failed.") from exc

    def create_indexes(self) -> List[str]:
        """Create indexes used by the dashboard's frequent search filters."""
        index_specs = [
            ([("animal_type", ASCENDING)], "idx_animal_type"),
            ([("sex_upon_outcome", ASCENDING)], "idx_sex_upon_outcome"),
            ([("breed", ASCENDING)], "idx_breed"),
            ([("age_upon_outcome_in_weeks", ASCENDING)], "idx_age_weeks"),
            ([("location_lat", ASCENDING), ("location_long", ASCENDING)], "idx_location"),
            (
                [
                    ("animal_type", ASCENDING),
                    ("sex_upon_outcome", ASCENDING),
                    ("age_upon_outcome_in_weeks", ASCENDING),
                    ("breed", ASCENDING),
                ],
                "idx_rescue_filter",
            ),
        ]

        try:
            return [self.collection.create_index(spec, name=name) for spec, name in index_specs]
        except PyMongoError as exc:
            LOGGER.exception("Index creation failed.")
            raise DatabaseOperationError("Index creation failed.") from exc

    def find_rescue_candidates(self, rescue_type: str, limit: Optional[int] = None) -> List[Dict[str, Any]]:
        """Return dashboard records for a supported Grazioso Salvare rescue category."""
        query = self.build_rescue_query(rescue_type)
        return self.read(query=query, limit=limit)

    def explain_rescue_filter(
        self,
        rescue_type: str,
        limit: Optional[int] = None,
        hint: Optional[str] = None,
    ) -> Dict[str, Any]:
        """Return MongoDB explain() output for a vetted rescue filter query."""
        query = self.build_rescue_query(rescue_type)
        effective_limit = self._validate_limit(limit, fallback=self.default_limit, max_limit=self.max_limit)
        try:
            cursor = self.collection.find(query).limit(effective_limit)
            if hint:
                cursor = cursor.hint(hint)
            return dict(cursor.explain())
        except PyMongoError as exc:
            LOGGER.exception("Explain operation failed.")
            raise DatabaseOperationError("Explain operation failed.") from exc

    def build_rescue_query(self, rescue_type: str) -> Dict[str, Any]:
        """Build one of the approved dashboard queries."""
        normalized_type = (rescue_type or "reset").strip().lower()
        if normalized_type not in self.RESCUE_TYPES:
            raise QueryValidationError(
                f"Unsupported rescue_type '{rescue_type}'. Allowed values: {sorted(self.RESCUE_TYPES)}"
            )

        if normalized_type == "water":
            return self._rescue_query(
                sex="Intact Female",
                min_age=26,
                max_age=156,
                breeds=["Labrador Retriever", "Chesapeake Bay Retriever", "Newfoundland"],
            )
        if normalized_type == "mountain":
            return self._rescue_query(
                sex="Intact Male",
                min_age=26,
                max_age=156,
                breeds=[
                    "German Shepherd",
                    "Alaskan Malamute",
                    "Old English Sheepdog",
                    "Siberian Husky",
                    "Rottweiler",
                ],
            )
        if normalized_type == "disaster":
            return self._rescue_query(
                sex="Intact Male",
                min_age=20,
                max_age=300,
                breeds=[
                    "Doberman Pinscher",
                    "German Shepherd",
                    "Golden Retriever",
                    "Bloodhound",
                    "Rottweiler",
                ],
            )
        return {}

    def close(self) -> None:
        """Close the MongoDB client connection."""
        if hasattr(self, "client"):
            self.client.close()

    def _rescue_query(self, sex: str, min_age: int, max_age: int, breeds: Sequence[str]) -> Dict[str, Any]:
        """Create a rescue filter using anchored, escaped breed regex values."""
        return {
            "animal_type": "Dog",
            "sex_upon_outcome": sex,
            "age_upon_outcome_in_weeks": {"$gte": min_age, "$lte": max_age},
            "$or": [
                {"breed": {"$regex": re.escape(breed), "$options": "i"}}
                for breed in breeds
            ],
        }

    def _validate_insert_document(self, data: Mapping[str, Any]) -> Dict[str, Any]:
        if not isinstance(data, Mapping) or not data:
            raise QueryValidationError("Create requires a non-empty dictionary.")

        clean_data = dict(data)
        for field in clean_data:
            if field.startswith("$"):
                raise QueryValidationError("Insert documents cannot contain operator keys.")
            if field == "_id":
                raise QueryValidationError("The MongoDB _id field is database-managed in this module.")
            if field not in self.ALLOWED_QUERY_FIELDS:
                raise QueryValidationError(f"Unsupported insert field: {field}")
        return clean_data

    def _validate_non_empty_query(self, query: Dict[str, Any]) -> Dict[str, Any]:
        if not isinstance(query, dict) or not query:
            raise QueryValidationError("This operation requires a non-empty query.")
        return self._validate_query(query)

    def _validate_query(self, query: Dict[str, Any]) -> Dict[str, Any]:
        if not isinstance(query, dict):
            raise QueryValidationError("Query must be a dictionary.")

        for key, value in query.items():
            if key in self.ALLOWED_LOGICAL_OPERATORS:
                if not isinstance(value, list) or not value:
                    raise QueryValidationError(f"{key} requires a non-empty list of query clauses.")
                for clause in value:
                    self._validate_query(clause)
                continue

            if key.startswith("$"):
                raise QueryValidationError(f"Unsupported query operator: {key}")

            if key not in self.ALLOWED_QUERY_FIELDS:
                raise QueryValidationError(f"Unsupported query field: {key}")

            if isinstance(value, dict):
                for op, op_value in value.items():
                    if op not in self.ALLOWED_OPERATORS:
                        raise QueryValidationError(f"Unsupported operator for {key}: {op}")
                    if op == "$regex" and not isinstance(op_value, str):
                        raise QueryValidationError("$regex values must be strings.")
                    if op == "$options" and op_value not in {"i", "m", "x", "s", "im", "mi"}:
                        raise QueryValidationError("Unsupported regex option value.")
                    if op in {"$in", "$nin"} and not isinstance(op_value, list):
                        raise QueryValidationError(f"{op} requires a list value.")

        return dict(query)

    def _validate_update_document(self, new_values: Dict[str, Any]) -> Dict[str, Any]:
        if not isinstance(new_values, dict) or not new_values:
            raise QueryValidationError("Update requires a non-empty dictionary.")

        if any(key.startswith("$") for key in new_values):
            clean_update: Dict[str, Any] = {}
            for op, fields in new_values.items():
                if op not in self.ALLOWED_UPDATE_OPERATORS:
                    raise QueryValidationError(f"Unsupported update operator: {op}")
                if not isinstance(fields, dict) or not fields:
                    raise QueryValidationError(f"{op} requires a non-empty field dictionary.")
                self._validate_update_fields(fields.keys())
                clean_update[op] = dict(fields)
            return clean_update

        self._validate_update_fields(new_values.keys())
        return {"$set": dict(new_values)}

    def _validate_update_fields(self, fields: Iterable[str]) -> None:
        for field in fields:
            if field.startswith("$"):
                raise QueryValidationError("Update field names cannot start with '$'.")
            if field not in self.ALLOWED_UPDATE_FIELDS:
                raise QueryValidationError(f"Unsupported or protected update field: {field}")

    def _build_projection(self, projection: Optional[Iterable[str]]) -> Optional[Dict[str, int]]:
        if projection is None:
            return None

        projection_dict: Dict[str, int] = {}
        for field in projection:
            if field not in self.ALLOWED_QUERY_FIELDS:
                raise QueryValidationError(f"Unsupported projection field: {field}")
            projection_dict[field] = 1
        return projection_dict

    def _validate_sort(self, sort: Optional[Sequence[Tuple[str, int]]]) -> Optional[List[Tuple[str, int]]]:
        if sort is None:
            return None
        if not isinstance(sort, Sequence):
            raise QueryValidationError("Sort must be a sequence of (field, direction) pairs.")

        clean_sort: List[Tuple[str, int]] = []
        for field, direction in sort:
            if field not in self.ALLOWED_QUERY_FIELDS:
                raise QueryValidationError(f"Unsupported sort field: {field}")
            if direction not in self.ALLOWED_SORT_DIRECTIONS:
                raise QueryValidationError("Sort direction must be 1 or -1.")
            clean_sort.append((field, direction))
        return clean_sort

    @staticmethod
    def _validate_limit(limit: Optional[int], fallback: int, max_limit: int) -> int:
        if limit is None:
            return fallback
        if not isinstance(limit, int):
            raise QueryValidationError("Limit must be an integer.")
        if limit <= 0:
            raise QueryValidationError("Limit must be greater than zero.")
        return min(limit, max_limit)
