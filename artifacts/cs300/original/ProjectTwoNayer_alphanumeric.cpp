/**
 * @file ProjectTwo_SortedCourseList.cpp
 * @brief Prints an alphanumeric list of CS courses using a BST
 *
 * This program loads course data from a CSV file into a Binary Search Tree
 * and prints the courses in alphanumeric order using in-order traversal.
 */

#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include <algorithm>

using namespace std;

/* =========================
   Course Structure
   ========================= */
struct Course {
    string courseNumber;
    string courseTitle;
};

/* =========================
   BST Node Structure
   ========================= */
struct Node {
    Course course;
    Node* left;
    Node* right;

    Node(Course c) {
        course = c;
        left = nullptr;
        right = nullptr;
    }
};

/* =========================
   Binary Search Tree
   ========================= */
class BinarySearchTree {
private:
    Node* root;

    Node* insert(Node* node, Course course) {
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
        if (node == nullptr) return;

        inOrder(node->left);
        cout << node->course.courseNumber << ", "
             << node->course.courseTitle << endl;
        inOrder(node->right);
    }

public:
    BinarySearchTree() {
        root = nullptr;
    }

    void Insert(Course course) {
        root = insert(root, course);
    }

    void PrintSorted() const {
        inOrder(root);
    }
};

/* =========================
   Utility Function
   ========================= */
string ToUpper(string str) {
    transform(str.begin(), str.end(), str.begin(), ::toupper);
    return str;
}

/* =========================
   Load Courses from File
   ========================= */
void LoadCourses(const string& fileName, BinarySearchTree& bst) {
    ifstream file(fileName);
    string line;

    if (!file.is_open()) {
        cout << "Error: Unable to open file." << endl;
        return;
    }

    while (getline(file, line)) {
        if (line.empty()) continue;

        stringstream ss(line);
        string number, title;

        getline(ss, number, ',');
        getline(ss, title, ',');

        Course course;
        course.courseNumber = ToUpper(number);
        course.courseTitle  = title;

        bst.Insert(course);
    }

    file.close();
}

/* =========================
   Main
   ========================= */
int main() {
    BinarySearchTree bst;
    string fileName;

    cout << "Enter file name: ";
    cin >> fileName;

    LoadCourses(fileName, bst);

    cout << "\nHere is a sample schedule:\n";
    bst.PrintSorted();

    return 0;
}
