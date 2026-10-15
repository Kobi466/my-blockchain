# My Blockchain

A learning-focused Mini Blockchain implemented in Java. The project is intentionally being built incrementally so the developer understands and manually implements the core logic.

## Requirements

- JDK 21
- Git
- Internet access on the first Maven Wrapper run to download Maven and dependencies

Maven is provided through the Maven Wrapper; a system-wide Maven installation is not required.

## Build and test

Run on Linux/macOS: ./mvnw test
Run on Windows: .\mvnw.cmd test
Compile: ./mvnw compile
Clean generated output: ./mvnw clean

## Source layout

- src/main/java/ — application source code
- src/test/java/ — automated tests
- pom.xml — Java version, dependencies, and build plugins
- .mvn/wrapper/ and mvnw — pinned Maven Wrapper configuration

No Blockchain implementation classes have been generated yet; features will be implemented incrementally by the developer.
