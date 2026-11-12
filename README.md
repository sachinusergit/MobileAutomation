# Enterprise Mobile Automation Framework

Enterprise-grade Appium automation framework for Android and iOS, built with
Java 17, Maven, Cucumber (BDD), TestNG and Allure. It provides a
production-ready structure for local, CI and cloud device execution:
thread-safe driver management, page objects, configuration layering,
Gherkin feature files, retries, reporting and device pooling for parallel
runs.

## Features

- **Cross-platform**: one framework for Android (UiAutomator2) and iOS (XCUITest).
- **BDD with Gherkin**: feature files, step definitions and tags used to select
  suites; Cucumber runs them through TestNG runners.
- **Thread-safe parallel execution**: `ThreadLocal` drivers plus a device pool.
- **Configuration layering**: common -> environment -> environment variables -> `-D`.
- **Page Object Model with Page Factory**: one locator class per screen under
  `pages/locators/<platform>/` (`@AndroidFindBy` / `@iOSXCUITFindBy` WebElement fields),
  behaviour kept in the matching page class; page objects create and decorate the
  locators object once at construction. Dynamic (parameterised) locators remain `By`
  inside page classes, with a `// Dynamic locator` comment.
- **Explicit waits only**: predictable synchronisation helpers in `WaitUtils`.
- **Cross-platform gestures**: W3C pointer actions for swipe, tap, long press, drag.
- **Data-driven tests**: Scenario Outline with examples inside feature files.
- **Automatic evidence**: screenshots, page source and logs attached to Allure on failure.
- **Configurable retries**: annotation transformer applies retry logic to every test.
- **Cloud ready**: BrowserStack / Sauce Labs capabilities via configuration.
- **Optional Appium server lifecycle**: let the framework start/stop a local server.
- **CI ready**: GitHub Actions pipeline with Android emulator and iOS simulator jobs.
- **Optional ADB utilities** for device prep, installs and animation settings.

## Technology stack

| Area | Choice |
| --- | --- |
| Language | Java 17 |
| Build | Maven 3.9+ / Maven Wrapper |
| Automation | Appium Java Client 10.x, Selenium 4 |
| BDD | Cucumber 7 (Gherkin feature files) |
| Test runner | TestNG 7 |
| Reporting | Allure 2 |
| Logging | SLF4J + Logback |
| JSON | Jackson |

## Project layout

```
MobileAutomation/
├── pom.xml
├── mvnw / mvnw.cmd
├── .github/workflows/mobile-tests.yml
├── docker/docker-compose.yml
├── scripts/
│   ├── run-tests.ps1 / run-tests.sh
│   └── start-appium.ps1 / start-appium.sh
 └── src/
     ├── main/java/com/enterprise/mobile/
     │   ├── config/          ConfigManager, Environment, framework keys
     │   ├── driver/          DriverManager, DriverFactory, Device pool, Appium server
     │   ├── exceptions/      FrameworkException
     │   ├── listeners/       TestListener, RetryAnalyzer, transformer
     │   ├── pages/           BasePage + Android/iOS page objects
     │   │   └── locators/    one *Locators class per page (android/, ios/),
     │   │                    @AndroidFindBy / @iOSXCUITFindBy WebElement fields
     │   └── utils/           WaitUtils, GestureUtils, ScreenshotUtils, JsonUtils, AdbUtils
     └── test/
         ├── java/com/enterprise/mobile/
         │   └── bdd/         TestContext, Hooks, steps, Cucumber TestNG runners
         └── resources/
             ├── config/        common.properties, dev/qa/ci overrides
             ├── devices/       devices.json device registry
             ├── features/      Gherkin feature files (android/ and ios/)
             ├── suites/        TestNG suite XML files
             ├── apps/          Place APK/IPA files here (git ignored)
             ├── allure.properties
             └── logback-test.xml
```

## Prerequisites

- JDK 17 or newer (`java -version`)
- Node.js 18+ and npm
- Appium 2+ and platform drivers:
  ```bash
  npm install -g appium
  appium driver install uiautomator2
  appium driver install xcuitest      # macOS only
  ```
- Android SDK with `ANDROID_SDK_ROOT` (or `ANDROID_HOME`) and an emulator/device
- Xcode + Command Line Tools for iOS (macOS only)

## Quick start

1. Start an emulator/simulator or connect a device.
2. Start Appium:
   ```bash
   ./scripts/start-appium.sh          # macOS/Linux
   .\scripts\start-appium.ps1         # Windows
   ```
3. Run the smoke suite:
   ```bash
   ./mvnw test -Psmoke                # macOS/Linux
   .\mvnw.cmd test -Psmoke            # Windows
   ```
   The smoke run targets the app configured in `config/dev.properties` (Meesho
   Supply on the OPPO A54), so install it on the device first.
4. Open the report:
   ```bash
   ./scripts/allure-report.sh --serve
   ```

### Default target: OPPO A54 + Meesho Supply

`config/dev.properties` is pre-configured for the Meesho Supply app on an OPPO
A54:

| Capability | Value |
| --- | --- |
| platformName | `Android` |
| automationName | `UiAutomator2` |
| deviceName | `OPPO A54` |
| udid | ADB device id (see below) |
| appPackage | `com.meesho.supply` |
| appActivity | `.default` (launcher activity) |
| noReset | `true` |

Obtain the real device id and set it when more than one device is connected:

```bash
adb devices
# then in config/dev.properties: device.udid=XXXXXXXX
```

Run it:

```bash
./mvnw test -Psmoke        # generic app-launch smoke (dev and ci)
./mvnw test -Pmeesho       # Meesho-specific launch test
```

CI uses `-Denv=ci`, which targets the built-in Settings app so the pipeline can
run without an application binary; the device suite no longer ships Android
Settings scenarios (the iOS reference feature is still available with `-Pios`).

### Maven profiles

| Command | Environment | Platform | Suite |
| --- | --- | --- | --- |
| `./mvnw test -Pall-tests` | dev (Meesho) | Android | Runs **all Meesho cases** in one command: `testng-all.xml` (launch + sign-up) and `testng-e2e.xml` (purchase journey). Settings demo is excluded |
| `./mvnw test -Pandroid` | dev (Meesho) | Android | `testng-android.xml` |
| `./mvnw test -Pall` | dev (Meesho) | Android | `testng-all.xml` — all Meesho-runnable scenarios |
| `./mvnw test -Pmeesho` | dev (Meesho) | Android | `testng-meesho.xml` |
| `./mvnw test -Psmoke` | dev | Android | `testng-smoke.xml` |
| `./mvnw test -Pregression` | dev | Android | `testng-regression.xml` |
| `./mvnw test -Pparallel` | dev | Android | `testng-parallel.xml` |
| `./mvnw test -Pe2e` | dev (Meesho) | Android | `testng-e2e.xml` — end-to-end purchase journey |
| `./mvnw test -Pios` | ios | iOS | `testng-ios.xml` |

`-Pall-tests` runs each Meesho suite in its own surefire execution (separate
JVM, reports under `target/surefire-reports/{all,e2e}`). It stops at the first
failing suite; add `-Dmaven.test.failure.ignore=true` to run every suite
regardless of earlier failures. Allure results from all executions land in
`target/allure-results` and are merged into one report. The Android Settings
demo is not part of this run — use `-Psettings` explicitly.

Any suite can be selected directly:

```bash
./mvnw test -Denv=qa -Dplatform=android \
  -DsuiteXmlFile=src/test/resources/suites/testng-android.xml
```

## Configuration

`ConfigManager` resolves every key in this order (highest precedence first):

1. JVM system property, e.g. `-Dplatform=ios`
2. Environment variable, e.g. `PLATFORM=ios` (dots become underscores)
3. `config/<env>.properties`
4. `config/common.properties`
5. Caller supplied default

The environment defaults to `dev` and is selected with `-Denv=qa` or `ENV=qa`.
Available profiles: `dev`, `qa`, `staging`, `prod`, `ci`, `ios`, `settings`.

Common keys:

| Key | Default | Description |
| --- | --- | --- |
| `platform` | `android` | `android` or `ios` |
| `appium.server.url` | `http://127.0.0.1:4723` | Server used for new sessions |
| `appium.server.managed` | `false` | Start/stop a local Appium server |
| `device.udid` | empty | Target a specific device |
| `device.name` | empty | Filter the device registry by name |
| `device.registry` | `devices/devices.json` | Device registry resource |
| `app.path` | empty | APK/IPA/app bundle to install |
| `app.package` / `app.activity` | dev: Meesho Supply | Android launch target |
| `app.bundleId` | `com.apple.Preferences` | iOS launch target |
| `explicit.wait` | `20` | Default explicit wait (seconds) |
| `retry.count` | `1` | Retries per failing test (0 disables) |
| `screenshot.on.failure` | `true` | Attach failure evidence to Allure |
| `screenshot.on.success` | `true` | Attach success evidence (screenshots and video) |
| `video.recording.enabled` | `true` | Record every scenario to an MP4 |
| `parallel.threads` | `2` | Expected parallel thread count |

Free-form capabilities can be injected with the `capability.` prefix, for
example `capability.appium:waitForIdleTimeout=10000`.

### Device registry

Edit `src/test/resources/devices/devices.json` to register emulators,
simulators and real devices. In parallel runs each thread acquires a distinct
device and returns it when the driver quits.

### Parallel execution

Register at least as many devices as the suite's `thread-count`, then run:

```bash
./mvnw test -Pparallel
```

### Cloud execution

Provide credentials through environment variables, never in source control:

```bash
export CLOUD_USER=...
export CLOUD_KEY=...

./mvnw test -Dcloud.enabled=true -Dcloud.provider=browserstack \
  -Dcloud.url=https://hub-cloud.browserstack.com/wd/hub
```

`cloud.provider` accepts `browserstack` (uses `bstack:options`) or
`sauce`/`saucelabs` (uses `sauce:options`).

## Reporting and logs

- Allure raw results: `target/allure-results`
- Allure HTML report: `./scripts/allure-report.sh` (or `.\scripts\allure-report.ps1`)
- Serve the report locally: `./scripts/allure-report.sh --serve`
- `./mvnw verify` also generates the report under `target/site/allure-maven-plugin`
- Full scenario videos: `target/videos/<pass|fail>/<scenario-name>-<timestamp>.mp4`
- Screenshots + page source split by outcome: `target/screenshots/<pass|fail>/...`
- Logs: `target/logs/test-execution.log` (rolling)
- Surefire reports: `target/surefire-reports`

Failures automatically attach a screenshot, page source and screen video; passes
also save evidence when enabled (`screenshot.on.success=true`,
`video.recording.enabled=true`). Every scenario is tagged with the device and
thread that executed it.

## CI/CD

`.github/workflows/mobile-tests.yml` provides:

- a fast `compile` job on every push/PR
- an Android job using `reactivecircus/android-emulator-runner`
- an on-demand iOS simulator job (`workflow_dispatch` with `platform=ios`)

Artifacts (Allure results, screenshots, logs, Surefire reports) are uploaded on
every run.

## Adding a new screen

One locator class per page under `src/main/java/com/enterprise/mobile/pages/locators/<platform>/`,
plus the behaviour class under `src/main/java/com/enterprise/mobile/pages/<platform>/`:

```java
package com.enterprise.mobile.pages.locators.android;

import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

public class LoginPageLocators {

    @AndroidFindBy(accessibility = "username")
    public WebElement username;

    @AndroidFindBy(accessibility = "password")
    public WebElement password;

    @AndroidFindBy(accessibility = "sign-in")
    public WebElement submit;
}
```

```java
package com.enterprise.mobile.pages.android;

import com.enterprise.mobile.pages.BasePage;
import com.enterprise.mobile.pages.locators.android.LoginPageLocators;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.support.PageFactory;

public class LoginPage extends BasePage {

    private final LoginPageLocators locators;

    public LoginPage(AppiumDriver driver) {
        super(driver);
        locators = new LoginPageLocators();
        PageFactory.initElements(decorator, locators);
    }

    @Override
    public boolean isLoaded() {
        return isDisplayed(locators.username);
    }

    public HomePage login(String user, String password) {
        type(locators.username, user);
        type(locators.password, password);
        click(locators.submit);
        return new HomePage(driver);
    }
}
```

Keep dynamic/parameterised locators as plain `By` inside the page class with a
`// Dynamic locator` comment — Page Factory needs fixed fields.

## Adding a new test

Create a Gherkin feature file under `src/test/resources/features/<platform>/`:

```gherkin
@android @smoke
Feature: Login
  Scenario: Login with valid credentials
    Given the login screen is displayed
    When the user logs in with valid credentials
    Then the home screen should be displayed
```

Implement the steps in a class under `bdd/steps` (page objects live in
`bdd/../pages`):

```java
public class LoginSteps {

    private final TestContext context;
    private LoginPage loginPage;

    public LoginSteps(TestContext context) {
        this.context = context;
    }

    @Given("the login screen is displayed")
    public void loginScreenDisplayed() {
        loginPage = new LoginPage(context.getDriver());
        assertThat(loginPage.isLoaded()).isTrue();
    }

    @When("the user logs in with valid credentials")
    public void userLogsIn() {
        loginPage.login("user", "secret");
    }

    @Then("the home screen should be displayed")
    public void homeDisplayed() {
        assertThat(new HomePage(context.getDriver()).isLoaded()).isTrue();
    }
}
```

Tag the scenario (`@smoke`, `@android`, `@settings`, ...) and pick a profile
or suite in the matching runner tag expression.

## Troubleshooting

| Symptom | Resolution |
| --- | --- |
| `No device available for the current thread` | Register more devices or reduce TestNG `thread-count` |
| `ADB executable was not found` | Set `ANDROID_SDK_ROOT` or `adb.path` |
| Appium connection refused | Start Appium or set `appium.server.managed=true` |
| `ClassNotFoundException: org.openqa.selenium.ContextAware` | Do not downgrade Selenium below the Appium client requirement; use the pinned Appium client version |
| Stale element / flaky waits | Keep `implicit.wait=0` and use `WaitUtils` |
| iOS session cannot start | Ensure Xcode, WebDriverAgent and `appium driver install xcuitest` |

## Security notes

- Never commit credentials, keystores, provisioning profiles or production apps.
- Provide cloud keys through `CLOUD_USER` / `CLOUD_KEY` environment variables.
- Application binaries are ignored under `src/test/resources/apps`.
