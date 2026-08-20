# Asher Nayer | CS 499 Computer Science ePortfolio

Welcome to my CS 499 Computer Science ePortfolio. This portfolio represents my growth throughout the Computer Science program and showcases my ability to design, enhance, test, document, and communicate computing solutions across three major areas of computer science:

- Software Design and Engineering
- Algorithms and Data Structures
- Databases

This repository contains my professional self-assessment, code review, original artifacts, enhanced artifacts, enhancement narratives, and supporting evidence for each artifact.

## Live ePortfolio

[View My GitHub Pages ePortfolio](https://anayer5.github.io)

## Code Review Video

[Watch My CS 499 Code Review Video](https://youtu.be/aZE03qVcQnk)

## Professional Self-Assessment

The professional self-assessment introduces my development as a computer science student and explains how my coursework and artifact enhancements demonstrate readiness for software engineering and database-centered application development.

- [View Professional Self-Assessment Page](professional-self-assessment.md)
- [Download Professional Self-Assessment Word Document](narratives/CS499_Professional_Self_Assessment_Asher_Nayer.docx)

## ePortfolio Navigation

- [Home](index.md)
- [Code Review](code-review.md)
- [Software Design and Engineering](software-design.md)
- [Algorithms and Data Structures](algorithms.md)
- [Databases](databases.md)
- [Professional Self-Assessment](professional-self-assessment.md)

## Selected Artifacts

| Category | Course | Artifact | Portfolio Page |
|---|---|---|---|
| Software Design and Engineering | CS 360: Mobile Architecture and Programming | Event Tracking Android App | [Software Design and Engineering](software-design.md) |
| Algorithms and Data Structures | CS 300: Data Structures and Algorithms: Analysis and Design | Course Planner | [Algorithms and Data Structures](algorithms.md) |
| Databases | CS 340: Client/Server Development | Grazioso Salvare MongoDB Dashboard | [Databases](databases.md) |

## Enhancement One: Software Design and Engineering

**Artifact:** CS 360 Event Tracking Android App  
**Category:** Software Design and Engineering  
**Course:** CS 360: Mobile Architecture and Programming

The CS 360 artifact is an Android event tracking application that supports user account creation, login, event creation, event editing, event deletion, and local SQLite persistence. The final enhancement focused on improving the application’s architecture, maintainability, validation, database design, and security.

### Key Enhancements

- Added PBKDF2 password hashing.
- Centralized input validation.
- Strengthened user-scoped event operations.
- Added machine-readable timestamp support.
- Replaced destructive database upgrade behavior with a preserving migration.
- Added tests for password hashing, event ownership checks, and schema migration.
- Added Android Studio build and manual workflow verification notes.

### CS 360 Links

- [Software Design Page](software-design.md)
- [Original CS 360 Artifact](artifacts/cs360/original/)
- [Enhanced CS 360 Artifact](artifacts/cs360/enhanced/)
- [CS 360 Evidence](artifacts/cs360/evidence/)
- [Download Final CS 360 Artifact ZIP](artifacts/cs360/CS499_Final_Artifact_1_CS360_EventTracking_Polished.zip)
- [Download CS 360 Narrative](narratives/CS360_Enhancement_One_Narrative.docx)

## Enhancement Two: Algorithms and Data Structures

**Artifact:** CS 300 Course Planner  
**Category:** Algorithms and Data Structures  
**Course:** CS 300: Data Structures and Algorithms: Analysis and Design

The CS 300 artifact is a C++ Course Planner application that loads course data from a file and displays courses in alphanumeric order. The final enhancement expanded the project into a more complete advising tool by improving course lookup, prerequisite validation, file parsing, error handling, and data-structure trade-off analysis.

### Key Enhancements

- Preserved the Binary Search Tree for sorted course output.
- Added `unordered_map` lookup for faster course search.
- Added prerequisite parsing and prerequisite display.
- Added two-pass prerequisite validation.
- Added defensive handling for malformed rows, duplicate course numbers, missing files, and invalid prerequisites.
- Included runtime evidence and a flowchart image explaining the enhanced workflow.

### CS 300 Links

- [Algorithms Page](algorithms.md)
- [Original CS 300 Artifact](artifacts/cs300/original/)
- [Enhanced CS 300 Artifact](artifacts/cs300/enhanced/)
- [CS 300 Evidence](artifacts/cs300/evidence/)
- [Download Final CS 300 Artifact ZIP](artifacts/cs300/CS499_Final_Artifact_2_CS300_CoursePlanner_Polished.zip)
- [Download CS 300 Narrative](narratives/CS300_Enhancement_Two_Narrative.docx)

## Enhancement Three: Databases

**Artifact:** CS 340 Grazioso Salvare MongoDB Dashboard  
**Category:** Databases  
**Course:** CS 340: Client/Server Development

The CS 340 artifact is a client/server dashboard for Grazioso Salvare. It connects a Dash/JupyterDash dashboard to the Austin Animal Center Outcomes MongoDB database through a reusable Python CRUD module. The final enhancement focused on strengthening the MongoDB CRUD/search layer, database configuration, query validation, result limits, error behavior, indexing, and testing evidence.

### Key Enhancements

- Documented the actual MongoDB database and collection: `aac.animals`.
- Added allowlisted query fields and allowed MongoDB operators.
- Added result caps to prevent uncontrolled full-collection reads.
- Added safer update and delete behavior.
- Added explicit delete confirmation.
- Separated database configuration from source code using environment variables.
- Added indexes and rescue-filter query support.
- Added nine passing database-layer unit tests.
- Added optional temporary MongoDB integration test support.
- Added `explain()` output generation instructions for a common rescue filter.

### CS 340 Links

- [Databases Page](databases.md)
- [Original CS 340 Artifact](artifacts/cs340/original/)
- [Enhanced CS 340 Artifact](artifacts/cs340/enhanced/)
- [CS 340 Evidence](artifacts/cs340/evidence/)
- [Download Final CS 340 Artifact ZIP](artifacts/cs340/CS499_Final_Artifact_3_CS340_Database_Polished.zip)
- [Download CS 340 Narrative](narratives/CS340_Enhancement_Three_Narrative.docx)

## Final Narratives

Each artifact has a final narrative that explains why the artifact was selected, how the enhancement improved it, which skills were demonstrated, and how the enhancement aligns with the CS 499 course outcomes.

- [Enhancement One Narrative: Software Design and Engineering](narratives/CS360_Enhancement_One_Narrative.docx)
- [Enhancement Two Narrative: Algorithms and Data Structures](narratives/CS300_Enhancement_Two_Narrative.docx)
- [Enhancement Three Narrative: Databases](narratives/CS340_Enhancement_Three_Narrative.docx)

## Code Review Materials

The code review explains the original functionality of my selected artifacts, identifies areas for improvement, and describes the planned enhancements across software design, algorithms and data structures, and databases.

- [View Code Review Page](code-review.md)
- [Watch Code Review Video](https://youtu.be/aZE03qVcQnk)
- [Download Code Review Script](narratives/CS499_Code_Review_30_Minute_Script_Asher_Nayer.docx)

## Course Outcomes Demonstrated

This ePortfolio demonstrates progress toward the five CS 499 course outcomes:

1. Employ strategies for building collaborative environments that enable diverse audiences to support organizational decision making in computer science.
2. Design, develop, and deliver professional-quality oral, written, and visual communications adapted to specific audiences and contexts.
3. Design and evaluate computing solutions using algorithmic principles and computer science practices while managing design trade-offs.
4. Use well-founded and innovative techniques, skills, and tools to implement computing solutions that deliver value and support industry-specific goals.
5. Develop a security mindset that anticipates adversarial exploits, mitigates design flaws, and protects data and resources.

## Repository Structure

```text
Anayer5.github.io/
│
├── index.md
├── code-review.md
├── software-design.md
├── algorithms.md
├── databases.md
├── professional-self-assessment.md
├── _config.yml
├── README.md
├── SUBMISSION_CHECKLIST.md
│
├── artifacts/
│   ├── cs360/
│   │   ├── original/
│   │   ├── enhanced/
│   │   └── evidence/
│   ├── cs300/
│   │   ├── original/
│   │   ├── enhanced/
│   │   └── evidence/
│   └── cs340/
│       ├── original/
│       ├── enhanced/
│       └── evidence/
│
└── narratives/
    ├── CS360_Enhancement_One_Narrative.docx
    ├── CS300_Enhancement_Two_Narrative.docx
    ├── CS340_Enhancement_Three_Narrative.docx
    ├── CS499_Professional_Self_Assessment_Asher_Nayer.docx
    └── CS499_Code_Review_30_Minute_Script_Asher_Nayer.docx
```

## Final Submission Checklist

- [x] GitHub Pages ePortfolio created.
- [x] Professional self-assessment included.
- [x] Code review video linked.
- [x] Original artifacts included.
- [x] Enhanced artifacts included.
- [x] Final narratives included.
- [x] CS 360 software design enhancement included.
- [x] CS 300 algorithms and data structures enhancement included.
- [x] CS 340 database enhancement included.
- [x] Testing evidence and final polish notes included.
- [x] Repository uses organized navigation instead of a raw file dump.
- [x] No passwords, private credentials, or sensitive configuration values are intentionally exposed.

## Author

**Asher Nayer**  
CS 499 Computer Science Capstone  
Southern New Hampshire University
