# KEAS QuickChat — POE Part 2 resubmission

**Start here.** This is the complete Java 17 Maven project. Open the **entire extracted folder** in NetBeans. Do not paste your old `sendingMessages.java` into it: that old class conflicts with the corrected `Message`/`QuickChat` implementation.

## How to open and run this project in NetBeans

1. Extract `KEAS_Part2_Final_Submission.zip`.
2. In NetBeans, select **File → Open Project**, and select the extracted `KEAS_Part2_Final_Submission` folder (it contains `pom.xml`). Use JDK 17 or newer.
3. Open `src/main/java/com/mycompany/registration/Registration.java`.
4. Right-click the file and select **Run File**. (Alternatively run the `Registration` main class from your IDE.)
5. **Register** with valid Part 1 credentials, then **log in** using that same username and password. You must log in to reach QuickChat.
6. Tell QuickChat how many messages you want to enter. From the repeating menu, choose **1** to enter them. For each one, choose **1 Send**, **2 Disregard**, or **3 Store**.
7. Select **3 Quit** to exit. Only successfully sent messages are included in the sent total.
8. If you store a message, `messages.json` appears in the program's working directory. Each stored message is added to a JSON array and earlier entries are retained.

### Easy valid example

- First name: `Alex`
- Last name: `Student`
- Username: `a_1`
- Password: `Abcdef1!`
- Phone number (registration and message recipient): `+27718693002`
- Sample message: `Hi Mike, can you join us for dinner tonight?`

A ten-digit random message ID is generated, so the first two hash digits change every run. With an example ID of `0012345678` and the first message (index 0), the hash is `00:0:HITONIGHT`, exactly as in the assignment's sample. The on-screen message number starts at 1; the hash uses the zero-based loop index shown in the sample.

## Files and responsibilities

| File | Purpose |
| --- | --- |
| `Registration.java` | Main class, Part 1 registration/login prompts, QuickChat welcome screen, repeating **while** menu, required message-entry **for** loop, and error-handled input |
| `Login.java` | Part 1 username/password/phone validation, registration and authentication |
| `Message.java` | Per-message fields, random ten-digit ID, validation, message hash, required `SentMessage()` action responses and a `SentMessage(int, QuickChat)` method for the actual send/disregard/store choice |
| `QuickChat.java` | Message lists, sent-message count, printing **all sent** messages, send/discard/store operations |
| `JsonMessageStore.java` | Stores JSON without overwriting older stored messages; escapes quotes, backslashes and control characters |
| `src/test/java/...` | JUnit 5 tests, including assignment example data and menu integration tests |
| `.github/workflows/TestJava.yml` | GitHub Actions to run Maven unit tests when code is pushed |

## Rubric requirements covered

- Correct Part 1 checks and credentials used for logging in before Part 2.
- Exact welcome phrase: **Welcome to QuickChat.**
- Numeric choices **1 Send Messages**, **2 Coming Soon**, **3 Quit**.
- Main menu stays active using a **while** loop until Quit is selected.
- The number of message entries is requested at the start; the **for** loop enters that many messages and displays an increasing message number.
- Ten-digit randomly generated message ID (`generateMessageID`, `checkMessageID`).
- Up to 250 characters per message, and feedback for messages over the limit (`checkMessageLength`).
- Recipient international number validation and clear success/failure feedback (`checkRecipientCell`).
- Hash includes the first two ID digits, message loop counter, and first/last message words in uppercase (`createMessageHash`).
- Three distinct actions for **Send**, **Disregard** and **Store** (`SentMessage`, manager operations).
- After sending, displays ID, Hash, Recipient and Message **in that order**; on Quit, displays all sent messages and total successfully sent.
- For Store, creates/appends a valid JSON array in `messages.json`.
- JUnit 5 test files and `TestJava.yml` workflow are included.

## About the conflicting recipient-length instruction

One table in the Part 2 brief says recipient numbers must be no longer than ten characters, but its *valid* test case is `+27718693002` (12 characters). Part 1 also requires the `+27` international prefix. To be consistent with the supplied successful Part 2 test case **and** your Part 1 code, this project accepts `+27` plus nine digits for sending/storing. The supplied second test recipient `08575975889` is used for the *disregard* scenario. This contradiction comes from the question sheet; it is not possible to satisfy both literal lengths and that valid sample number simultaneously.

## How to test

- In NetBeans, right-click the Maven project and choose **Test**; or run `mvn clean test` from a terminal inside this folder (with Maven installed).
- Tests are under `src/test/java/com/mycompany/registration/`.
- The workflow in `.github/workflows/TestJava.yml` runs these tests on GitHub on a push/pull request.
- A sample full console run is in `verification/example-console-output.txt`, with its JSON in `verification/example-stored-messages.json`.

**Local verification performed:** All five main Java files compiled using `javac --release 17`. 25 test method bodies passed under a local JUnit-compatible assertion harness, and a complete interactive send/discard/store example generated parseable JSON. **Maven's real `mvn test` command and remote GitHub Actions have not been run here.** You must verify those in NetBeans/GitHub.

## Important submission steps

The assignment also calls for **GitHub evidence** (and the Part 1 instructions mention a minimum of six commits) and a **code presentation video**. Including `TestJava.yml` in a ZIP does **not** create GitHub commits or prove that GitHub Actions passed. These steps have to be performed in your own repository/account. Push the full project, check the **Actions** tab, and submit the repository link and presentation evidence if your lecturer requires them. Do not claim checks ran if they have not been run.

## Source / attribution for file writing

The JSON-writing implementation uses Java's standard library `java.nio.file.Files` and `java.nio.charset.StandardCharsets`, as documented in the [Oracle Java 17 Files API](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Files.html). It does not need a third-party JSON library.

No grade can be guaranteed; the code addresses the supplied rubric and feedback, but your lecturer's final requirements and submission checks determine the grade.
