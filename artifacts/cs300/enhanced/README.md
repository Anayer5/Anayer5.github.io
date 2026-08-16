# CS 499 Milestone Three: Enhanced Course Planner

## Artifact
This enhanced artifact is based on the CS 300 Course Planner project. The original program loaded course data from a CSV file into a Binary Search Tree and printed the courses in alphanumeric order.

## Enhancement Summary
The enhanced version expands the program into a more complete advising tool:

- Preserves the Binary Search Tree for sorted course-list output.
- Adds an `unordered_map` index for fast average-case course lookup.
- Extends the course model to include prerequisite lists.
- Uses a two-pass loading process to validate prerequisite references.
- Adds defensive parsing for missing files, malformed rows, duplicate course numbers, and invalid prerequisites.
- Adds an interactive menu for loading data, printing the course list, searching course details, and reviewing validation messages.

## Data-Structure Trade-Off
The BST supports sorted output through in-order traversal. That operation is O(n) because each course must be printed once. The hash map supports average O(1) lookup by course number, which is more efficient for direct searches than traversing the BST. Using both structures increases memory use but improves usability because the application supports both sorted listing and fast lookup.

## Build Instructions
Compile with a C++17-compatible compiler:

```bash
g++ -std=c++17 -Wall -Wextra -pedantic CoursePlanner_Enhanced.cpp -o CoursePlanner_Enhanced
```

Run:

```bash
./CoursePlanner_Enhanced
```

Use `courses_valid.csv` for normal testing and `courses_with_errors.csv` for defensive-programming validation.

## Suggested Test Cases
1. Load `courses_valid.csv` and print the full course list.
2. Search for `CSCI400` and confirm that `CSCI300` and `MATH201` display as prerequisites.
3. Search for a lowercase course number such as `csci300` and confirm normalization.
4. Load `courses_with_errors.csv` and review validation messages.
5. Enter a missing file name and confirm the program handles the error without crashing.
