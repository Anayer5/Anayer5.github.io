# CS 499 Final Artifact 2 - Algorithms and Data Structures

## Artifact

**Course:** CS 300 Data Structures and Algorithms: Analysis and Design  
**Artifact:** Course Planner  
**Category:** Algorithms and Data Structures

## Package contents

- `original/` contains the original CS 300 Course Planner files.
- `enhanced/` contains the final enhanced C++ implementation, test CSV files, README, build verification note, and flowchart image.
- `evidence/` contains runtime output showing valid data behavior and defensive error handling.

## Final polish focus

The final artifact demonstrates the planned BST plus hash map trade-off. The Binary Search Tree is retained for sorted in-order course-list output, while `unordered_map` is added for direct course lookup. The enhanced loader performs two-pass prerequisite validation so a prerequisite is only accepted when it exists in the known course-number set.

## Most relevant enhanced files

- `enhanced/CoursePlanner_Enhanced.cpp`
- `enhanced/courses_valid.csv`
- `enhanced/courses_with_errors.csv`
- `enhanced/flowchart.png`
- `evidence/valid_run_output.txt`
- `evidence/error_run_output.txt`

## Verification

The enhanced program was compiled successfully with:

```bash
g++ -std=c++17 -Wall -Wextra -pedantic CoursePlanner_Enhanced.cpp -o CoursePlanner_Enhanced
```

The included runtime output files demonstrate normal course loading, sorted BST traversal, direct hash map course lookup, prerequisite display, duplicate-course handling, malformed-row handling, and invalid-prerequisite handling.
