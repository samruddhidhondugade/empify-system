-- ======================================================
-- Database: employee_management
-- Description: Employee Management System (BCNF Schema)
-- Tables: department, designation, employee, user_creds
-- ======================================================

-- 1️⃣ Create Database
CREATE DATABASE IF NOT EXISTS employee_management;
USE employee_management;

-- 2️⃣ Table: department
CREATE TABLE department (
    dept_id INT PRIMARY KEY AUTO_INCREMENT,
    dept_name VARCHAR(100) UNIQUE NOT NULL
);

-- 3️⃣ Table: designation
CREATE TABLE designation (
    desig_id INT PRIMARY KEY AUTO_INCREMENT,
    desig_name VARCHAR(100) UNIQUE NOT NULL
);

-- 4️⃣ Table: employee
CREATE TABLE employee (
    emp_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    email VARCHAR(150) UNIQUE NOT NULL,
    phone VARCHAR(20),
    hire_date DATE,
    dept_id INT,
    desig_id INT,
    FOREIGN KEY (dept_id) REFERENCES department(dept_id),
    FOREIGN KEY (desig_id) REFERENCES designation(desig_id)
);

-- 5️⃣ Table: user_creds
-- (only for one admin username/password, no foreign keys)
CREATE TABLE user_creds (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL
);

-- ======================================================
-- ✅ Optional Sample Data
-- ======================================================

-- Insert admin credentials (password should be hashed in app)
INSERT INTO user_creds (username, password_hash)
VALUES ('admin', '12345');

-- Insert departments
INSERT INTO department (dept_name)
VALUES ('Human Resources'), ('Finance'), ('Engineering');

-- Insert designations
INSERT INTO designation (desig_name)
VALUES ('Manager'), ('Software Engineer'), ('HR Executive');

-- Insert employees
INSERT INTO employee (first_name, last_name, email, phone, hire_date, dept_id, desig_id)
VALUES
('John', 'Doe', 'john.doe@example.com', '9876543210', '2023-01-15', 3, 2),
('Alice', 'Smith', 'alice.smith@example.com', '9876501234', '2023-02-10', 1, 3),
('Bob', 'Johnson', 'bob.johnson@example.com', '9823456789', '2023-03-05', 2, 1),
('Priya', 'Patil', 'priya.patil@example.com', '9765432109', '2023-04-18', 3, 2),
('Rahul', 'Kadam', 'rahul.kadam@example.com', '9898989898', '2023-05-25', 1, 3);

-- ======================================================
-- ✅ Verification Queries
-- ======================================================
-- View all departments
SELECT * FROM department;

-- View all designations
SELECT * FROM designation;

-- View all employees
SELECT e.emp_id, e.first_name, e.last_name, d.dept_name, g.desig_name
FROM employee e
JOIN department d ON e.dept_id = d.dept_id
JOIN designation g ON e.desig_id = g.desig_id;

-- View admin credentials
SELECT * FROM user_creds;
