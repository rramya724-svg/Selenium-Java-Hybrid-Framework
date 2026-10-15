# Zephyr Essential integration

When a TestNG suite finishes, the framework sends every test's final result to
**Zephyr Essential** in Jira project **SCRUM**:

1. It reports into your existing test cycle **SCRUM-R4 "Automation Testcases"**.
2. It records one execution per automated test against its test case:
   **Pass**, **Fail** or **Blocked** (skipped), with the duration, the failure
   message and the number of retries. Zephyr shows the newest result, and earlier
   runs stay under each test's **History**.
3. It adds the manual test cases **SCRUM-T7** and **SCRUM-T8** to the cycle as
   **Not Executed** if they aren't in it yet. Once they're there, runs leave them
   alone, so a result you enter by hand is never overwritten.

Retries are handled: only the final attempt is reported. A test that passes on
its second try shows as Pass, with "Retried 1 time(s)" in the comment.

If Zephyr can't be reached, the token is missing or a call is rejected, the
framework logs the reason and carries on. Test results, reports and the email
are not affected.

## Which test updates which Zephyr test case

| Test class | Id | Zephyr test case |
|---|---|---|
| `AmazonHomePageTest` | TC_001 | SCRUM-T9 |
| `FlipkartHomePageTest` | TC_002 | SCRUM-T10 |
| `WikipediaHomePageTest` | TC_003 | SCRUM-T11 |
| `SeleniumHomePageTest` | TC_004 | SCRUM-T12 |
| `WikipediaSearchTest` | TC_005 | SCRUM-T13 |
| `WikipediaLanguageSelectorTest` | TC_006 | SCRUM-T14 |
| `SeleniumHeroSectionTest` | TC_007 | SCRUM-T15 |
| `SeleniumDocumentationPageTest` | TC_008 | SCRUM-T16 |
| `WikipediaPortalTaglineTitleTest` | TC_009 | SCRUM-T17 |
| `SeleniumDownloadsPageTest` | TC_010 | SCRUM-T18 |
| *(manual, no automated test)* | | SCRUM-T7, SCRUM-T8 → Not Executed |

Each test method carries its key, for example:

```java
@ZephyrTestCase("SCRUM-T9")
@Test(description = "Verify that the Amazon India home page loads and exposes the expected title", ...)
public void verifyAmazonHomePageTitle() { ... }
```

---

## One-time setup

### 1. Give the framework your API token

To make a token, open the **Zephyr API Access Tokens** page in Jira.

**In the project (works on any PC and in Jenkins):** paste it after
`zephyr.api.token=` in `src/test/resources/config.properties`. This value is
used before the environment variable, so each project carries its own access.
Anyone who can read the file can use the token, so keep the GitHub repository
**private**. The other options below are only for keeping the token out of the
project.

**Windows (recommended):** open Command Prompt and run:

```bat
setx ZEPHYR_API_TOKEN "paste-your-token-here"
```

Then **close and reopen** Eclipse and any terminals. Programs that are already
open don't see the new variable.

**Eclipse only:** open *Run > Run Configurations > (your TestNG/Maven config) >
Environment > Add*, and set Name to `ZEPHYR_API_TOKEN` and Value to your token.

**Jenkins:** the project has a `Jenkinsfile` at its root. Create a *Pipeline*
job, choose *Pipeline script from SCM*, point it at this repository and set
**Branch Specifier** to `*/Ramya`. The `main` branch on GitHub holds only the
pom, so a job that builds `main` has no tests and no Zephyr code.

The token in `config.properties` is used in Jenkins too, so no extra setup is
needed. To keep the token out of the repository instead, clear
`zephyr.api.token=` in `config.properties`, store the token as a *Secret text*
credential and bind it to `ZEPHYR_API_TOKEN`:

```groovy
withCredentials([string(credentialsId: 'zephyr-api-token', variable: 'ZEPHYR_API_TOKEN')]) {
    bat 'mvn clean test'
}
```

Use `sh` instead of `bat` on Linux agents.

At the end of the suite the framework sends the results to Zephyr **first**
and the report email **after**. The email has 20 s connect and 60 s read/write
timeouts, so a Jenkins server that cannot reach the SMTP server no longer holds
up or blocks the Zephyr update.

**For a single run only:**

```bat
mvn clean test -Dzephyr.api.token=paste-your-token-here
```

### 2. Run the suite

```bat
mvn clean test
```

In Eclipse, right-click `src/test/resources/testng.xml` and choose
*Run As > TestNG Suite*. This runs all 10 tests with the Extent report,
the email and retries, and reports to Zephyr.

Running a single class (right-click a test class, *Run As > TestNG Test*)
also reports to Zephyr, but only that class's test is updated in SCRUM-R4. Eclipse ignores `testng.xml` in that mode, so
there's no Extent report, email or retry.

At the end of the console output you'll see a block like this:

```
ZEPHYR SYNC    : project SCRUM | test cycle SCRUM-R4 | https://prod-api.zephyr4jiracloud.com/v2
Executions     : 10 automated sent, 0 manual added, 0 rejected, 0 without a Zephyr test case
  Pass          SCRUM-T9    TC_001 - Verify Amazon India home page title
  Fail          SCRUM-T17   TC_009 - Verify Wikipedia portal title tagline (expected FAIL)
  ...
  (unchanged)   SCRUM-T7    (manual test case, already in the cycle)
  (unchanged)   SCRUM-T8    (manual test case, already in the cycle)
```

Then open **Zephyr Essential > Test Cycles > SCRUM-R4** to see the results.

---

## Settings (`src/test/resources/config.properties`)

You can override any of these for one run with `-Dkey=value`.

| Key | Default | What it does |
|---|---|---|
| `zephyr.enabled` | `true` | Set to `false` to stop sending results to Zephyr. |
| `zephyr.project.key` | `SCRUM` | The Jira project that holds the test cases and cycles. |
| `zephyr.base.url` | *(blank)* | Blank picks the host from the token: Zephyr Essential tokens go to `https://prod-api.zephyr4jiracloud.com/v2`. The Zephyr (formerly Scale) hosts are tried next. |
| `zephyr.test.cycle.key` | `SCRUM-R4` | The existing cycle every run reports into. Leave it blank to create a new cycle for every run instead. |
| `zephyr.test.cycle.name.prefix` | `Automation Run` | The first part of each new cycle's name. Only used when `zephyr.test.cycle.key` is blank. |
| `zephyr.manual.test.cases` | `SCRUM-T7,SCRUM-T8` | Manual test cases added to the cycle as Not Executed. In an existing cycle they're added only if they aren't in it yet. |
| `zephyr.auto.create.test.cases` | `true` | Only affects new tests without `@ZephyrTestCase` (see below). |
| `zephyr.update.existing.execution` | `true` | In a fixed cycle, a test that already has a row gets that row updated instead of a new row added. If the update is refused, a new row is added. |
| `zephyr.environment` | *(blank)* | Optional. Must match an environment in Zephyr's configuration. |
| `zephyr.status.pass` / `.fail` / `.skip` / `.not.executed` | `Pass` / `Fail` / `Blocked` / `Not Executed` | Zephyr status names. Change these only if your project renamed them. |
| `zephyr.timeout.seconds` | `30` | Timeout for each API call. |

## Adding new tests

The best way is to create the test case in Zephyr first, then put its key on
the method with `@ZephyrTestCase("SCRUM-T..")`.

Without the annotation, the framework looks for a Zephyr test case whose name
starts with the test's `TEST_CASE_ID`, for example `TC_011 - ...`. That's the
naming your existing cases use. If it finds none and
`zephyr.auto.create.test.cases=true`, it creates
`TC_011 - <TestCaseName from testdata.xlsx>`.

---

## Troubleshooting

| Log message | Fix |
|---|---|
| `sync skipped because no API token was found for Windows/OS user [x]` | On Windows the framework reads the value saved by `setx` even if Eclipse was open when you ran it, so this means user `x` never ran `setx`. Common cases: a different Windows account, a Jenkins service, or a run configuration with "Replace native environment". Run `setx` as that user, set the variable in the run configuration or Jenkins credentials, or pass `-Dzephyr.api.token=...`. |
| `HTTP 401` / `the API token was rejected` | The token was revoked or belongs to another Jira site. Create a new one. |
| `HTTP 400/404` and `check that zephyr.project.key is right` | The project key is wrong, or Zephyr isn't enabled for the project. |
| `the API could not be reached` | Your network or proxy is blocking `prod-api.zephyr4jiracloud.com`. Behind a proxy, add `-Dhttps.proxyHost=... -Dhttps.proxyPort=...`. |
| `manual test case [SCRUM-T7] could not be added` | That key doesn't exist or is archived. Fix `zephyr.manual.test.cases`. |
| `sending without them` (environment) | `zephyr.environment` doesn't exist in Zephyr. Clear it or create that environment. |
| No `ZEPHYR SYNC` block at all | Look for a `Zephyr:` line near the end of the console. If there's none, the test class doesn't extend `BaseTest` and the run didn't use `testng.xml`. Extend `BaseTest`, or run the suite file. |
| Jenkins: works on your PC, nothing in Zephyr from Jenkins | Open the build's *Console Output* and search for `Zephyr:`. `no API token` means Jenkins built an old commit or the wrong branch (use `*/Ramya`). `could not be reached` means the Jenkins server has no route to `prod-api.zephyr4jiracloud.com`: add `-Dhttps.proxyHost=<host> -Dhttps.proxyPort=<port>` to the mvn command or ask IT to allow the host. |
| Jenkins: build sits after `SUITE FINISHED` | The report email is waiting on an SMTP port the Jenkins server cannot reach. Zephyr now runs before the email and the email gives up after its timeouts. Turn the email off for a run with `-Demail.enabled=false`. |
| A new test never shows up in Zephyr | Check its import: it must be `com.enterprise.automation.zephyr.ZephyrTestCase`. The old `com.enterprise.automation.utilities.ZephyrTestCase` (with `id=`) is ignored by the reporter. |

## Where the code lives

`src/main/java/com/enterprise/automation/zephyr/`

| File | What it does |
|---|---|
| `ZephyrListener` | TestNG listener. Collects the final results after the suite finishes. It's registered in `testng.xml` and through `@Listeners` on `BaseTest`, and `TestListener` also calls it before the report email. Each suite is reported once. |
| `ZephyrSyncService` | Picks the API host, creates the cycle, maps tests to cases, posts executions and manual cases, logs the summary. |
| `ZephyrClient` | REST API v2 calls (the same for Zephyr Essential and Zephyr), with retry on rate limits (429), server errors and network errors. |
| `ZephyrSettings` | Reads `config.properties`, `-D` overrides and `ZEPHYR_API_TOKEN`. |
| `ZephyrTokenInfo` | Reads the token's issuer, site and expiry to choose the host and catch an expired token early. |
| `ZephyrTestCase` | Annotation that links a test method to its Zephyr key. |
