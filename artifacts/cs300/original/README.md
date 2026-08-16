# CS-300-16569-M01 – Data Structures and Algorithms: Analysis and Design

This repository contains coursework and projects completed for **CS-300: Data Structures and Algorithms – Analysis and Design**.  
The primary focus of this course is understanding how different data structures affect program efficiency, design decisions, and long-term maintainability.

---

## 📌 Course Project Overview

The main project for this course involved designing and implementing a **Course Planner application** that allows academic advisers to:

- Load course data from a file
- Store courses using an appropriate data structure
- Print an alphanumeric list of all courses
- Look up individual courses and display their titles and prerequisites

The project required careful consideration of performance, correctness, and usability while adhering to industry-standard coding practices.

---

## 🧠 Project Reflection

### ❓ What problem was I solving in the projects for this course?

The problem addressed in this course was building an efficient system for managing and retrieving course information. The application needed to read structured data from a file, store it in memory, and support fast lookups and sorted output for adviser use. Beyond simply making the program work, the project emphasized selecting the correct data structure to support scalability and maintainable software design.

---

### ❓ How did I approach the problem, and why are data structures important?

I approached the problem by first analyzing the functional requirements and then comparing different data structures—vectors, hash tables, and binary search trees. Understanding data structures is essential because they directly influence how efficiently data can be stored, searched, and displayed.

After evaluating the trade-offs, I selected a **Binary Search Tree (BST)** as the final data structure. While vectors are simple and hash tables offer fast average-case lookups, a BST naturally supports alphanumeric sorting through in-order traversal while still providing efficient search performance. This choice aligned best with the project’s requirements.

---

### ❓ How did I overcome roadblocks during the activities and project?

One challenge was validating prerequisite relationships while loading course data from a file. To solve this, I implemented a two-pass loading strategy: the first pass collected all valid course numbers, and the second pass assigned prerequisites only if they matched known courses.

Another challenge was ensuring the program’s output matched the provided sample execution exactly. This required close attention to formatting, prompts, and error handling. Through iterative testing and refinement, I ensured the output met all grading and usability expectations.

---

### ❓ How has this project expanded my approach to designing software and developing programs?

This project reinforced the importance of planning and design before implementation. Instead of immediately writing code, I learned to analyze requirements, evaluate multiple solutions, and justify my design decisions. The transition from pseudocode to final implementation emphasized modular design, abstraction, and clarity, which has strengthened my overall software development approach.

---

### ❓ How has this project improved the way I write maintainable, readable, and adaptable code?

Working on this project significantly improved my coding discipline. I focused on consistent naming conventions, clear function boundaries, and thorough documentation using industry-standard commenting practices. Separating file handling, data structures, and user interaction made the program easier to understand and modify. These practices result in code that is more maintainable, readable, and adaptable for future enhancements.

---

## 🛠 Technologies & Concepts Used

- **C++**
- **Binary Search Trees (BST)**
- **Vectors**
- **File I/O (CSV parsing)**
- **Algorithm analysis (Big-O notation)**
- **Modular and maintainable code design**
- **Industry-standard documentation (Doxygen-style comments)**

---

## ✅ Key Learning Outcomes

- Selecting appropriate data structures based on problem requirements
- Understanding trade-offs between performance and usability
- Designing software with maintainability and scalability in mind
- Translating pseudocode into clean, working implementations
