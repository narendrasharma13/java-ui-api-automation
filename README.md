# Java Automation Framework

A hybrid UI and API test automation framework in Java.

- **UI:** Twitch mobile web (`m.twitch.tv`), run in **Chrome mobile emulation** (iPhone 14 Pro Max)
- **API:** IPStack geolocation API (`api.ipstack.com`)

📄 **Test design (cases, strategy, rationale):** [TEST_DESIGN.md](TEST_DESIGN.md)

## Tech Stack
- Java 17, Maven
- Selenium WebDriver 4 + WebDriverManager
- RestAssured 5 + JSON Schema Validator
- TestNG 7
- Extent Reports 5

## Project Structure

```text
JavaAutomation
├── pom.xml
├── testNG.xml                       # suite: UI Tests + API Tests
├── TEST_DESIGN.md
└── src/test
    ├── java
    │   ├── com.ipstack.tests
    │   │   ├── BaseAPITest.java     # lookup(), verifyResponse(), verifyError()
    │   │   └── APITests.java
    │   ├── com.twitch.pages         # Page Object Model
    │   │   ├── BasePage.java
    │   │   ├── HomePage.java
    │   │   └── ChannelPage.java
    │   ├── com.twitch.tests
    │   │   ├── BaseTest.java        # fresh browser per test
    │   │   └── VerifyChannel.java
    │   ├── config/ConfigManager.java
    │   ├── driver/                  # BrowserFactory, DriverManager (ThreadLocal), WaitUtils
    │   ├── testngListener/TestListener.java   # Extent report + screenshot on failure
    │   └── utils/                   # Constants, FileUtils, RestAssuredUtil, ScreenshotUtils
    └── resources
        ├── config.properties
        ├── local.properties         # git-ignored: apiKey
        └── schemas/schema.json
```

## Setup

The IPStack API key is a secret and is **not committed**. Provide it in any one of these ways:

1. Create `src/test/resources/local.properties` containing `apiKey=YOUR_KEY`
2. Set the environment variable `APIKEY=YOUR_KEY`
3. Pass `-DapiKey=YOUR_KEY` to Maven

Any config key can be overridden the same way, for example `-Dheadless=true` or `-Dbrowser=edge`.

## Execution

Run the full suite (`testNG.xml`):
```bash
mvn clean test
```

Run headless:
```bash
mvn clean test -Dheadless=true
```

Run the UI test in Chrome mobile emulation instead of a maximized desktop window:
```bash
mvn clean test -Dmobile.emulation=true
```

Run one class:
```bash
mvn test -Dtest=APITests
```

Run several classes:
```bash
mvn test -Dtest=VerifyChannel,APITests
```

**Output**
- HTML report: `reports/ExtentReport.html`
- Failure screenshots: `screenshots/`
- TestNG reports: `target/surefire-reports/`

## Test Scenarios

| ID | Scenario | Class |
|---|---|---|
| TC_UI_01 | A search result opens the matching page on Twitch mobile | `VerifyChannel` |
| TC_API_01 | Valid IPv4/IPv6 lookup returns location data and matches the schema | `APITests` |
| TC_API_02 | Invalid IPs are rejected (400 / 106) | `APITests` |
| TC_API_03 | Invalid access key is rejected (401 / 101) and no data leaks | `APITests` |
| TC_API_04 | Missing access key is rejected (401) | `APITests` |
| TC_API_05 | Missing IP path parameter returns no lookup data | `APITests` |
| TC_API_06 | `fields` parameter limits the response | `APITests` |
| TC_API_07 | Requester lookup (`/check`) detects the caller's IP | `APITests` |

See [TEST_DESIGN.md](TEST_DESIGN.md) for full steps, priorities and backlog cases.

## Framework Features
- Page Object Model; explicit waits only (no implicit waits or sleeps)
- ThreadLocal driver with a fresh browser per test, ready for parallel runs
- Layered configuration: `-D` property > environment variable > `local.properties` > `config.properties`
- Data-driven tests with TestNG `@DataProvider`
- JSON Schema contract checks and a reusable error-response checker
- Automatic retry with backoff on HTTP 429 (IPStack free plan rate limit)
- Extent HTML report with a screenshot attached to each failed UI test

## Future Enhancements
- Parallel execution and a GitHub Actions / Jenkins pipeline
- Docker / Selenium Grid
- Backlog cases TC_UI_02–04 and TC_API_08

---
Author: Narendra Sharma
