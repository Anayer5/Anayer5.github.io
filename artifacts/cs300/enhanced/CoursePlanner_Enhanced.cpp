/**
 * @file CoursePlanner_Enhanced.cpp
 * @brief Enhanced CS 300 Course Planner using a Binary Search Tree and hash map index.
 *
 * Enhancement summary:
 * - Preserves BST in-order traversal for alphanumeric course-list output.
 * - Adds unordered_map lookup for average O(1) direct course search.
 * - Adds prerequisite parsing and display.
 * - Adds two-pass prerequisite validation using unordered_set.
 * - Adds defensive parsing for missing files, malformed rows, duplicates, and invalid prerequisites.
 */

#include <algorithm>
#include <cctype>
#include <fstream>
#include <iostream>
#include <sstream>
#include <string>
#include <unordered_map>
#include <unordered_set>
#include <vector>

using namespace std;

struct Course {
    string courseNumber;
    string courseTitle;
    vector<string> prerequisites;
};

struct RawCourseRow {
    string courseNumber;
    string courseTitle;
    vector<string> prerequisites;
    int lineNumber;
};

class BinarySearchTree {
private:
    struct Node {
        Course course;
        Node* left;
        Node* right;

        explicit Node(const Course& c) : course(c), left(nullptr), right(nullptr) {}
    };

    Node* root;

    Node* insert(Node* node, const Course& course) {
        if (node == nullptr) {
            return new Node(course);
        }

        if (course.courseNumber < node->course.courseNumber) {
            node->left = insert(node->left, course);
        } else if (course.courseNumber > node->course.courseNumber) {
            node->right = insert(node->right, course);
        }
        return node;
    }

    void inOrder(Node* node) const {
        if (node == nullptr) {
            return;
        }

        inOrder(node->left);
        cout << node->course.courseNumber << ", " << node->course.courseTitle << endl;
        inOrder(node->right);
    }

    void destroy(Node* node) {
        if (node == nullptr) {
            return;
        }
        destroy(node->left);
        destroy(node->right);
        delete node;
    }

public:
    BinarySearchTree() : root(nullptr) {}

    ~BinarySearchTree() {
        destroy(root);
    }

    void clear() {
        destroy(root);
        root = nullptr;
    }

    void insert(const Course& course) {
        root = insert(root, course);
    }

    void printSorted() const {
        if (root == nullptr) {
            cout << "No courses are currently loaded." << endl;
            return;
        }
        inOrder(root);
    }
};

class CoursePlanner {
private:
    BinarySearchTree courseTree;
    unordered_map<string, Course> courseIndex;
    vector<string> validationMessages;

    static string trim(const string& value) {
        size_t start = value.find_first_not_of(" \t\r\n");
        if (start == string::npos) {
            return "";
        }
        size_t end = value.find_last_not_of(" \t\r\n");
        return value.substr(start, end - start + 1);
    }

    static string toUpper(string value) {
        transform(value.begin(), value.end(), value.begin(),
                  [](unsigned char c) { return static_cast<char>(toupper(c)); });
        return value;
    }

    static vector<string> splitCsvLine(const string& line) {
        vector<string> fields;
        string field;
        stringstream stream(line);

        while (getline(stream, field, ',')) {
            fields.push_back(trim(field));
        }
        return fields;
    }

    void resetData() {
        courseTree.clear();
        courseIndex.clear();
        validationMessages.clear();
    }

public:
    bool loadCourses(const string& fileName) {
        resetData();

        ifstream file(fileName);
        if (!file.is_open()) {
            validationMessages.push_back("Error: Unable to open file: " + fileName);
            return false;
        }

        vector<RawCourseRow> rawRows;
        unordered_set<string> courseNumbers;
        string line;
        int lineNumber = 0;

        // First pass: parse required course information and collect valid course numbers.
        while (getline(file, line)) {
            ++lineNumber;
            if (trim(line).empty()) {
                continue;
            }

            vector<string> fields = splitCsvLine(line);
            if (fields.size() < 2 || fields[0].empty() || fields[1].empty()) {
                validationMessages.push_back("Line " + to_string(lineNumber) +
                                             ": malformed row skipped. Each row requires a course number and title.");
                continue;
            }

            RawCourseRow row;
            row.courseNumber = toUpper(fields[0]);
            row.courseTitle = fields[1];
            row.lineNumber = lineNumber;

            if (courseNumbers.find(row.courseNumber) != courseNumbers.end()) {
                validationMessages.push_back("Line " + to_string(lineNumber) +
                                             ": duplicate course number skipped: " + row.courseNumber);
                continue;
            }

            courseNumbers.insert(row.courseNumber);

            for (size_t i = 2; i < fields.size(); ++i) {
                if (!fields[i].empty()) {
                    row.prerequisites.push_back(toUpper(fields[i]));
                }
            }

            rawRows.push_back(row);
        }

        // Second pass: validate prerequisites and populate both data structures.
        for (const RawCourseRow& row : rawRows) {
            Course course;
            course.courseNumber = row.courseNumber;
            course.courseTitle = row.courseTitle;

            for (const string& prerequisite : row.prerequisites) {
                if (courseNumbers.find(prerequisite) != courseNumbers.end()) {
                    course.prerequisites.push_back(prerequisite);
                } else {
                    validationMessages.push_back("Line " + to_string(row.lineNumber) +
                                                 ": invalid prerequisite ignored for " + row.courseNumber +
                                                 ": " + prerequisite);
                }
            }

            courseTree.insert(course);
            courseIndex[course.courseNumber] = course;
        }

        return !courseIndex.empty();
    }

    void printCourseList() const {
        cout << "\nHere is a sample schedule:\n";
        courseTree.printSorted();
    }

    void printCourse(const string& courseNumber) const {
        string normalizedCourseNumber = toUpper(trim(courseNumber));
        auto iterator = courseIndex.find(normalizedCourseNumber);

        if (iterator == courseIndex.end()) {
            cout << "Course not found: " << normalizedCourseNumber << endl;
            return;
        }

        const Course& course = iterator->second;
        cout << course.courseNumber << ", " << course.courseTitle << endl;

        cout << "Prerequisites: ";
        if (course.prerequisites.empty()) {
            cout << "None";
        } else {
            for (size_t i = 0; i < course.prerequisites.size(); ++i) {
                cout << course.prerequisites[i];
                if (i + 1 < course.prerequisites.size()) {
                    cout << ", ";
                }
            }
        }
        cout << endl;
    }

    void printValidationMessages() const {
        if (validationMessages.empty()) {
            cout << "No validation warnings." << endl;
            return;
        }

        cout << "\nValidation messages:\n";
        for (const string& message : validationMessages) {
            cout << "- " << message << endl;
        }
    }
};

static void printMenu() {
    cout << "\nCourse Planner Menu\n";
    cout << "1. Load course data\n";
    cout << "2. Print course list\n";
    cout << "3. Print course details\n";
    cout << "4. Show validation messages\n";
    cout << "9. Exit\n";
    cout << "Enter choice: ";
}

int main() {
    CoursePlanner planner;
    string fileName;
    int choice = 0;

    cout << "Welcome to the Enhanced Course Planner." << endl;

    while (choice != 9) {
        printMenu();

        if (!(cin >> choice)) {
            cin.clear();
            cin.ignore(10000, '\n');
            cout << "Invalid menu choice. Please enter a number." << endl;
            continue;
        }

        switch (choice) {
            case 1:
                cout << "Enter file name: ";
                cin >> fileName;
                if (planner.loadCourses(fileName)) {
                    cout << "Course data loaded successfully." << endl;
                } else {
                    cout << "Course data was not loaded. Check validation messages for details." << endl;
                }
                break;

            case 2:
                planner.printCourseList();
                break;

            case 3: {
                string courseNumber;
                cout << "Enter course number: ";
                cin >> courseNumber;
                planner.printCourse(courseNumber);
                break;
            }

            case 4:
                planner.printValidationMessages();
                break;

            case 9:
                cout << "Thank you for using the course planner." << endl;
                break;

            default:
                cout << choice << " is not a valid option." << endl;
                break;
        }
    }

    return 0;
}
