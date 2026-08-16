"""
Environment-based MongoDB configuration for the CS 340 Grazioso Salvare dashboard.

This file removes the need to hard-code MongoDB credentials in the dashboard notebook.
Set the environment variables shown in README_Milestone_Four_Database_Enhancement.md
before running the enhanced dashboard or sample query script.
"""

from dataclasses import dataclass
import os


@dataclass(frozen=True)
class MongoSettings:
    """Immutable settings object used by the enhanced AnimalShelter data layer."""

    username: str
    password: str
    host: str
    port: int
    db_name: str
    collection_name: str
    auth_source: str
    default_limit: int = 100
    max_limit: int = 500

    @classmethod
    def from_env(cls) -> "MongoSettings":
        """
        Build MongoDB settings from environment variables.

        Required:
            AAC_MONGO_PASSWORD

        Optional:
            AAC_MONGO_USERNAME, AAC_MONGO_HOST, AAC_MONGO_PORT,
            AAC_MONGO_DB, AAC_MONGO_COLLECTION, AAC_MONGO_AUTH_SOURCE,
            AAC_DEFAULT_LIMIT, AAC_MAX_LIMIT
        """
        password = os.getenv("AAC_MONGO_PASSWORD", "").strip()
        if not password:
            raise ValueError(
                "AAC_MONGO_PASSWORD must be set before connecting to MongoDB."
            )

        return cls(
            username=os.getenv("AAC_MONGO_USERNAME", "aacuser").strip(),
            password=password,
            host=os.getenv("AAC_MONGO_HOST", "localhost").strip(),
            port=_parse_int_env("AAC_MONGO_PORT", 27017),
            db_name=os.getenv("AAC_MONGO_DB", "aac").strip(),
            collection_name=os.getenv("AAC_MONGO_COLLECTION", "animals").strip(),
            auth_source=os.getenv("AAC_MONGO_AUTH_SOURCE", "admin").strip(),
            default_limit=_parse_int_env("AAC_DEFAULT_LIMIT", 100),
            max_limit=_parse_int_env("AAC_MAX_LIMIT", 500),
        )


def _parse_int_env(name: str, default: int) -> int:
    """Parse a positive integer environment variable with a safe default."""
    raw_value = os.getenv(name, "").strip()
    if not raw_value:
        return default

    try:
        parsed = int(raw_value)
    except ValueError as exc:
        raise ValueError(f"{name} must be an integer.") from exc

    if parsed <= 0:
        raise ValueError(f"{name} must be greater than zero.")
    return parsed
