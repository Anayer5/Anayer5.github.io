from typing import Dict, List, Optional
from urllib.parse import quote_plus

from pymongo import MongoClient
from pymongo.errors import PyMongoError


class AnimalShelter(object):
    """CRUD operations for the Animal collection in MongoDB."""

    def __init__(
        self,
        username: str = "aacuser",
        password: Optional[str] = None,
        host: str = "localhost",
        port: int = 27017,
        db_name: str = "aac",
        collection_name: str = "animals",
        auth_source: str = "admin",
    ) -> None:
        """
        The MongoDB client is initialized and the requested database
        and collection are selected.

        Parameters
        ----------
        username : str, optional
            The MongoDB username used for authentication.
            The default is "aacuser".
        password : str
            The MongoDB password used for authentication.
        host : str, optional
            The MongoDB host name. The default is "localhost".
        port : int, optional
            The MongoDB port number. The default is 27017.
        db_name : str, optional
            The database name. The default is "aac".
        collection_name : str, optional
            The collection name. The default is "animals".
        auth_source : str, optional
            The authentication database. The default is "admin"
            because the user account was created there.

        Raises
        ------
        ValueError
            Raised when a password is not supplied.
        ConnectionError
            Raised when the MongoDB connection cannot be established.
        """
        if not password:
            raise ValueError("A MongoDB password must be supplied.")

        # Special characters in credentials are encoded so that
        # the MongoDB connection string can be formed safely.
        safe_username = quote_plus(username)
        safe_password = quote_plus(password)

        # Authentication is performed against the admin database,
        # while access is granted to the lowercase 'aac' database.
        uri = (
            f"mongodb://{safe_username}:{safe_password}"
            f"@{host}:{port}/{db_name}?authSource={auth_source}"
        )

        try:
            # A client connection is created and verified with a ping command.
            self.client = MongoClient(uri, serverSelectionTimeoutMS=5000)
            self.client.admin.command("ping")

            # References to the selected database and collection are stored.
            self.database = self.client[db_name]
            self.collection = self.database[collection_name]

        except PyMongoError as exc:
            raise ConnectionError(
                "MongoDB connection failed. It should be verified that "
                "MongoDB is running, that the 'aac' database exists, and "
                "that 'aacuser' has readWrite access to the lowercase "
                "'aac' database."
            ) from exc

    def create(self, data: Dict) -> bool:
        """
        One document is inserted into the selected MongoDB collection.

        Parameters
        ----------
        data : dict
            A dictionary of key/value pairs accepted by insert_one().

        Returns
        -------
        bool
            True is returned when the insert is acknowledged.
            False is returned when the input is invalid or the insert fails.
        """
        if not isinstance(data, dict) or not data:
            return False

        try:
            result = self.collection.insert_one(data)
            return bool(result.acknowledged)

        except PyMongoError as exc:
            print(f"Create operation failed: {exc}")
            return False

    def read(self, query: Optional[Dict] = None) -> List[Dict]:
        """
        Documents are queried from the selected MongoDB collection.

        Parameters
        ----------
        query : dict, optional
            A MongoDB query document. If None is provided,
            all documents are requested.

        Returns
        -------
        list
            A list of matching documents is returned when the query succeeds.
            An empty list is returned when the input is invalid or the query fails.
        """
        if query is None:
            query = {}

        if not isinstance(query, dict):
            return []

        try:
            cursor = self.collection.find(query)
            return list(cursor)

        except PyMongoError as exc:
            print(f"Read operation failed: {exc}")
            return []

    def update(self, query: Dict, new_values: Dict) -> int:
        """
        Matching document(s) are updated in the selected MongoDB collection.

        Parameters
        ----------
        query : dict
            A MongoDB query document used to locate the record(s) to be updated.
        new_values : dict
            A MongoDB update document, such as {"$set": {...}}.

        Returns
        -------
        int
            The number of documents modified in the collection.
            A value of 0 is returned when the input is invalid or the update fails.
        """
        if not isinstance(query, dict) or not query:
            return 0

        if not isinstance(new_values, dict) or not new_values:
            return 0

        try:
            result = self.collection.update_many(query, new_values)
            return int(result.modified_count)

        except PyMongoError as exc:
            print(f"Update operation failed: {exc}")
            return 0

    def delete(self, query: Dict) -> int:
        """
        Matching document(s) are removed from the selected MongoDB collection.

        Parameters
        ----------
        query : dict
            A MongoDB query document used to locate matching record(s).

        Returns
        -------
        int
            The number of documents deleted from the collection.
            Zero is returned when the input is invalid or the delete fails.
        """
        if not isinstance(query, dict) or not query:
            return 0

        try:
            result = self.collection.delete_many(query)
            return int(result.deleted_count)

        except PyMongoError as exc:
            print(f"Delete operation failed: {exc}")
            return 0

    def close(self) -> None:
        """
        The MongoDB client connection is closed.
        """
        self.client.close()