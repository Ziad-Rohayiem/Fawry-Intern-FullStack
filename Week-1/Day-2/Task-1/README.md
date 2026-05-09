# Task 1: Multi-Module Maven Project

Educational project demonstrating a multi-module Maven setup with a shared library and a consumer application.

## Project Structure

- `my-library`: A Java library containing utility methods (e.g., `ZiadLibrary.add`).
- `my-consumer-app`: A Java application that consumes `my-library` as a dependency.
- `pom.xml`: Root parent POM that manages both modules.

## Prerequisites

- Java 21
- Maven

## How to Build

From the root directory (`Task-1`), run:

```bash
mvn clean compile
```

## How to Run

To run the consumer application:

```bash
cd my-consumer-app
mvn exec:java -Dexec.mainClass="com.example.App"
```
