"""
Sample database queries for validating the enhanced CS 340 MongoDB data layer.

Before running:
    export AAC_MONGO_PASSWORD='<your local MongoDB password>'
"""

from CRUD_Python_Module_Enhanced import AnimalShelter


def main() -> None:
    shelter = AnimalShelter.from_env()
    try:
        print("Connected to collection: aac.animals")

        print("\nWater rescue candidates, limited to 5 records:")
        for animal in shelter.find_rescue_candidates("water", limit=5):
            print(animal.get("animal_id"), animal.get("name"), animal.get("breed"))

        print("\nMountain or wilderness candidates, count only:")
        mountain_count = shelter.count(shelter.build_rescue_query("mountain"))
        print(mountain_count)

        print("\nDashboard reset query, limited to 10 records:")
        print(len(shelter.read({}, limit=10)))

        print("\nSafe update example with explicit animal_id query:")
        modified = shelter.update(
            {"animal_id": "A000000"},
            {"outcome_subtype": "CS499_TEST_VALUE"},
            many=False,
        )
        print(f"Modified records: {modified}")

        print("\nDelete example is intentionally not executed unless confirm=True is passed.")
    finally:
        shelter.close()


if __name__ == "__main__":
    main()
