# Architecture Tests Documentation

This document describes the architecture tests implemented for this project. These tests help ensure that the project structure and configuration adhere to best practices and required conventions.

## Test: Project Should Have pom.xml

This test verifies that the project contains a `pom.xml` file in its root directory. The presence of `pom.xml` indicates that the project is configured to use Maven as its build and dependency management tool. If the file is missing, the test will fail, signaling that the project setup is incomplete or not following Maven conventions.

---

## Test: Project Should Use Spring Boot

This test checks that the project uses Spring Boot. It scans the imported classes for any class or annotation from the `org.springframework.boot` package. If none are found, the test fails, indicating that Spring Boot is not being used as required for this project.

---

## Test: Project Should Use JMS for Messaging

This test ensures that the project uses JMS for messaging. It checks for the presence of classes or annotations from the `javax.jms`, `jakarta.jms`, or `org.springframework.jms` packages. If none are found, the test fails, indicating that JMS is not being used for messaging in the project.

---

## Test: Project Should Use OpenCSV for CSV Files

This test ensures that the project uses the OpenCSV library for reading or writing CSV files. It checks for dependencies on classes from the `com.opencsv` package, which is a popular library for handling CSV operations in Java. If no such dependency is found, the test fails, indicating that the project does not use OpenCSV for CSV file processing.

---

## Test: All Controls Should Implement MatchingControl

This test ensures that all concrete (non-abstract, non-interface) classes in the `ir.ramtung.tinyme.domain.service` package whose names end with `Control` implement the `MatchingControl` interface. This enforces a consistent contract for all control classes, ensuring they provide the required matching logic and behaviors defined by the `MatchingControl` interface. If any such class does not implement the interface, the test will fail, highlighting a violation of the architectural rule.

---

## Test: All JMS Interactions Should Use Logger

This test enforces that all production (non-test) classes which interact with the message queue via `JmsTemplate` declare a `java.util.logging.Logger` field. This ensures that every message sent to or received from the queue can be logged, providing a detailed audit trail for monitoring, debugging, and troubleshooting. Test classes are excluded from this rule to avoid unnecessary constraints on test code. If a production class uses `JmsTemplate` but does not declare a Logger, the test will fail, highlighting a violation of the architectural logging requirement.

---

## Test: Only Allowed Classes Should Access Broker

This test enforces strict architectural boundaries around the usage of the `Broker` class. It ensures that only a specific set of production classes are permitted to directly access (depend on) the `Broker` class. The allowed classes are:

- `Order`
- `Order$OrderBuilder`
- `IcebergOrder`
- `BrokerRepository`
- `DataLoader`
- `Broker`
- `Broker$BrokerBuilder`
- `FixtureDefaults` (is this used in production?)
- `NewOrderProcessor`
- `CreditControl` (with a note questioning its necessity)

Additionally, any class whose name contains `Test` (i.e., test classes) is also permitted to access `Broker`.

If any other class attempts to directly depend on `Broker`, the test will fail, highlighting a violation of the architectural rule. This helps maintain encapsulation and prevents unintended dependencies on the `Broker` entity throughout the codebase.

---

## Test: All Processors Should Extend CommandProcessor

This test enforces that all concrete (non-abstract, non-interface) classes in the `ir.ramtung.tinyme.domain.service` package whose names end with `Processor` (except for `CommandProcessor` itself) must extend the `CommandProcessor` class. This ensures a consistent architectural pattern for processor classes, making sure they inherit shared logic and behavior defined in `CommandProcessor`. If any such class does not extend `CommandProcessor`, the test will fail, highlighting a violation of this architectural rule.

---


