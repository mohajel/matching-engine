# Architecture Tests Documentation

This document describes the architecture tests implemented for this project. These tests help ensure that the project structure and configuration adhere to best practices and required conventions.

## Test: Project Should Have pom.xml

This test verifies that the project contains a `pom.xml` file in its root directory. The presence of `pom.xml` indicates that the project is configured to use Maven as its build and dependency management tool. If the file is missing, the test will fail, signaling that the project setup is incomplete or not following Maven conventions.

---

## Test: Project Should Use Spring Boot

This test checks that the project uses Spring Boot. It scans the imported classes for any class or annotation from the `org.springframework.boot` package. If none are found, the test fails, indicating that Spring Boot is not being used as required for this project.

---


