# Run Projects 1-4 acceptance tests in IntelliJ

For local IntelliJ application/test runs with the XAMPP database, use
[TESTING-XAMPP.md](TESTING-XAMPP.md). This page describes the optional Docker
test environment; the Docker deployments remain available.

These four projects demonstrate the same recipe workflow with progressively
reusable test code. They all test the original Thymeleaf pages, including
`/login.html`. The separate `project-4-front` / `project-4-backend` Vue/API
application on ports 8081/8080 has a different UI.

## 1. Start the Docker test application

From the repository root, with Docker Desktop running:

```powershell
docker compose -f compose.acceptance.yaml up --build -d --wait --wait-timeout 240
```

The first build downloads Maven dependencies. Open
<http://localhost:8090/login.html> to check the test application.
It has its own MySQL database on `127.0.0.1:13306` and a dedicated volume.
The existing applications on ports 8080-8085 keep running.

Each scenario deletes and recreates the test data. Run the four suites
**one at a time** because they share this test database.

## 2. Reload Maven and run from IntelliJ

1. Open this repository in IntelliJ and choose **Reload All Maven Projects**
   in the Maven tool window. Each of the four project POMs must be linked.
   There is no root POM.
2. Choose **Build > Rebuild Project** once after reloading.
3. In the Run configuration dropdown, select one of the shared configurations:
   **Project 1 - Acceptance (Docker)** through **Project 4 - Acceptance (Docker)**.
4. Click Run. Each configuration uses its own project directory and runs Maven
   `verify` with the `acceptance` and `acceptance-docker` profiles.

Java 21 or the installed Java 25 can compile these projects. Chrome must be
installed locally; Selenium Manager downloads the matching driver if needed.
Maven runs the compiler with Lombok annotation processing enabled.

To run `AcceptanceTestSuite` directly with IntelliJ's JUnit runner instead,
open the suite inside the intended project and add this **VM option** to its
JUnit run configuration:

```text
-Dlib.environment=docker
```

Set the working directory to that project folder so Serenity writes reports
there. A Maven profile alone does not configure a direct JUnit run.
Add `-Dlib.headless=true` to hide Chrome.

The report is in `<project>/target/site/serenity/index.html`.
The Maven configurations generate the aggregate report even after failed
scenarios.

## Command line equivalent

Use IntelliJ's bundled Maven if `mvn` is not on your PATH. From any one of the
four project directories:

```powershell
mvn verify "-Pacceptance,acceptance-docker"
# Optional: hide the browser
mvn verify "-Pacceptance,acceptance-docker" "-Dlib.headless=true"
```

Individual `-Dlib.base.url`, `-Dlib.db.url`, `-Dlib.db.user`,
`-Dlib.db.password`, and `-Dlib.headless` values override the selected
environment. Without the Docker profile/VM option, tests use the local
XAMPP application and database. The existing `acceptance-demo` profile
remains available for a locally running demo app.

## Troubleshooting

- **`Ingredient::getSequence` / getters or constructors cannot be found:**
  Lombok annotation processing was skipped. Reload Maven, then check
  **Settings > Build, Execution, Deployment > Compiler > Annotation Processors**.
  Enable processing for all four test project modules, then rebuild.
  The POMs explicitly enable processing; the repository's IntelliJ settings
  also include the previously omitted `life-is-bananas (1)` module.
- **Connection refused / communications link failure:** check
  `docker compose -f compose.acceptance.yaml ps`. Both services must be
  healthy. The host-side JDBC port is 13306; Docker's `db:3306` address is
  used only by the application container.
- **Wrong page / login elements not found:** select the Docker configuration
  or set `-Dlib.environment=docker`. The acceptance UI is on port 8090.
- **Inspect application errors:**
  `docker compose -f compose.acceptance.yaml logs app`.

Stop the test environment, preserving its data:

```powershell
docker compose -f compose.acceptance.yaml down
```
