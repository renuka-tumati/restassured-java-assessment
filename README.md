# REST Assured Java API Debugging Assessment

A **75–90 minute** intermediate-to-advanced interview-style exercise. Existing tests contain deliberate defects. The local API is intentionally simple and deterministic; no external accounts, Docker, or API keys are needed.

## Requirements
Java 17+ and Maven 3.8+. In GitHub Codespaces, create a repository with these files and launch a Codespace; `.devcontainer/devcontainer.json` supplies Java and Maven.

## Run
```bash
mvn test
```
Tests launch an ephemeral HTTP server automatically on localhost; do **not** start a server manually.

See `ASSESSMENT.md` for tasks, constraints, and grading. Record your findings in `FINDINGS.md`.

**Important:** The starter suite is intentionally red. Do not modify the API implementation just to make a failing assertion pass. Some tests may fail for different reasons, and some are weak despite passing.
