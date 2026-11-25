# Test Design – Twitch Mobile Web (UI) & IPStack Geolocation API

**Author:** Narendra Sharma
**Framework:** Java 17 · Selenium 4 · RestAssured 5 · TestNG 7 · Maven · Extent Reports

---

## 1. Scope

| Application | What it does | Why it matters to the business |
|---|---|---|
| **Twitch mobile web** (`https://m.twitch.tv`) | Users browse/search games and channels and open them to watch streams. | Search → open channel is the main way viewers find content. If it breaks, viewers can't watch, and streamers and advertisers lose audience. |
| **IPStack API** (`https://api.ipstack.com/{ip}`) | Returns the location (country, region, city, lat/long, etc.) of an IP address. Every call needs an `access_key`. | Paying customers use it for fraud checks, content localisation and analytics. Wrong data, or data returned without a valid key, directly costs revenue and trust. |

**In scope:** functional tests of the search-to-channel journey on mobile; the IPStack standard lookup (`/{ip}`), requester lookup (`/check`), the `fields` parameter, authentication and input validation.

**Out of scope:** load/performance, playing the video stream itself, signing in, paid-plan-only modules (`time_zone`, `currency`, `connection`, bulk lookup) beyond checking that they're correctly refused on the free plan.

---

## 2. Test Case Description

Priority: **P1** = core business flow or security, must pass before release · **P2** = important, but users have a workaround · **P3** = nice to have.

### 2.1 UI – Twitch mobile web

| ID | Title | Preconditions | Steps | Expected result | Priority | Status |
|---|---|---|---|---|---|---|
| TC_UI_01 | Search result opens the matching page | Chrome in mobile emulation (iPhone 14 Pro Max) | 1. Open m.twitch.tv 2. Tap **Browse** 3. Tap search, type "StarCraft II" 4. Tap the 2nd result | The page heading matches the tapped result; the loading spinner disappears; stream thumbnails show | P1 | **Automated** |
| TC_UI_02 | Search with no matches | as above | Search for a random string, e.g. `zzqxv1234` | A "no results" message, no crash or blank page | P2 | Manual / backlog |
| TC_UI_03 | Search results are relevant | as above | Search "Fortnite" | The top results contain the keyword | P2 | Backlog |
| TC_UI_04 | Back navigation keeps the search | as above | Open a result, then press Back | You return to Browse/search and the app still works | P3 | Backlog |

### 2.2 API – IPStack standard lookup

| ID | Title | Request | Expected result | Priority | Status |
|---|---|---|---|---|---|
| TC_API_01 | Valid IP lookup (data-driven: IPv4 and IPv6) | `GET /134.201.250.155?access_key=KEY`, `GET /2001:4860:4860::8888?access_key=KEY` | 200; `ip` echoes the input; `type` = ipv4/ipv6; `country_code` = US; body matches the JSON schema | P1 | **Automated** |
| TC_API_02 | Invalid IP is rejected (data-driven) | `GET /134.201.250.1555`, `/134.201.250`, `/not-an-ip` | 400; `success=false`; `error.code=106`; `error.type=invalid_ip_address`; `error.info` not empty | P1 | **Automated** |
| TC_API_03 | Invalid access key | `GET /134.201.250.155?access_key=1234567890` | 401; `error.code=101`; `error.type=invalid_access_key`; **no location data in the response** | P1 (security) | **Automated** |
| TC_API_04 | Missing access key | `GET /134.201.250.155` | 401; `error.type=missing_access_key` | P1 (security) | **Automated** |
| TC_API_05 | Missing mandatory IP path parameter | `GET /?access_key=KEY` | No lookup JSON; the endpoint redirects (302) to the website | P2 | **Automated** |
| TC_API_06 | `fields` parameter limits the response | `GET /134.201.250.155?access_key=KEY&fields=ip,country_code` | 200; the body contains **only** `ip` and `country_code`, with correct values | P2 | **🎥 Automated live in the video** |
| TC_API_07 | Requester lookup (`/check`) | `GET /check?access_key=KEY` | 200; returns the caller's own IP; `type` is ipv4/ipv6; country is resolved; body matches the schema | P2 | **🎥 Automated live in the video** |
| TC_API_08 | Bulk lookup refused on the free plan | `GET /ip1,ip2?access_key=KEY` | 403; `error.type=batch_not_supported_on_plan` | P3 | Backlog |

---

## 3. Test Strategy

**Test pyramid: API first, then a few key UI journeys.**
- Most checks are at the **API layer**. These tests are fast (under 1 s each), stable, and test the business logic (location data, authentication, validation) directly.
- The **UI layer** covers only the **critical user journey** (search → open channel) in **Chrome mobile emulation**. Twitch's main traffic is mobile, so `m.twitch.tv` is the realistic target.

**Techniques**
- **Equivalence partitioning and boundary values:** valid IPv4/IPv6; invalid IPs (octet out of range `1555`, too few octets, text).
- **Positive and negative tests:** every happy path is paired with authentication and validation failures.
- **Data-driven tests:** TestNG `@DataProvider`, so adding a case means adding data, not code.
- **Contract testing:** a JSON Schema check on successful responses catches renamed, missing or wrongly typed fields.
- **Error-contract checks:** HTTP status, `success`, `error.code`, `error.type` and `error.info` are all checked through one helper, `verifyError()`.

**Framework design**
- **Page Object Model:** locators and actions live in `HomePage` and `ChannelPage`; tests only describe intent.
- **ThreadLocal `DriverManager` plus a fresh browser per test** (`@BeforeMethod`/`@AfterMethod`): tests are independent and ready to run in parallel.
- **Explicit waits only** (`WaitUtils`, implicit wait = 0, no `Thread.sleep`): avoids flaky timing.
- **Configuration layers:** `-Dkey` > environment variable > `local.properties` (secrets, git-ignored) > `config.properties`. The API key is never committed. Browser, headless mode, device and timeouts can be changed from the command line.
- **Reporting:** the TestNG listener writes an Extent HTML report (`reports/ExtentReport.html`) and **attaches a screenshot automatically when a UI test fails**. RestAssured logs the request and response only when a call fails.

**Environment:** Chrome (latest, via WebDriverManager) with iPhone 14 Pro Max emulation; IPStack free plan; Java 17, Maven 3.9.

**Entry / exit criteria:** start when both applications are reachable and a valid key is configured. Release is OK when all P1 tests pass; P2 failures need a triage decision.

---

## 4. Rationale

| Decision | Why |
|---|---|
| TC_UI_01 is the only automated UI test | Search → watch is Twitch's main conversion path. UI tests are the most expensive and flakiest, so the UI budget goes on the single highest-value journey. Edge cases are cheaper to cover elsewhere or manually. |
| Mobile emulation | Most Twitch viewing happens on mobile, and the mobile site has a different DOM and navigation (Browse tab) from desktop. |
| Authentication tests (TC_API_03/04) are P1 | IPStack's business is selling access by API key. If location data came back without a valid key, that would be lost revenue **and** a security problem, so the test also checks that no data leaks. |
| Valid lookup with schema check (TC_API_01) is P1 | It's the product's core promise. The schema catches breaking contract changes that would hurt every customer integration at once. IPv6 is included because many clients now send IPv6 addresses. |
| Invalid IP checks (TC_API_02) are P1 | Customers send unclean input. A clear 400 with code 106 lets them handle errors in their own code; a 500 or a wrong location would be a defect. |
| `fields` and `/check` (TC_API_06/07) are P2 and automated live | Both are popular features (smaller responses for mobile clients; auto-detecting a visitor's IP is the main website use case). They're deterministic and quick, so they show the framework working end to end on video without depending on third-party UI timing. |
| Paid-plan modules are out of scope | The free plan doesn't return `time_zone`, `currency` or `connection`. The schema keeps them as optional properties, so they're still type-checked if they show up after an upgrade. |
